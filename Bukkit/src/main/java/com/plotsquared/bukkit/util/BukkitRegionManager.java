/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.generator.AugmentedUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.PlotLoc;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.plotsquared.core.util.entity.EntityCategories.CAP_ANIMAL;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ENTITY;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MISC;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MOB;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MONSTER;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_VEHICLE;

@Singleton
public class BukkitRegionManager extends RegionManager {

    private static final Logger logger =
        LoggerFactory.getLogger("P2/" + BukkitRegionManager.class.getSimpleName());
    private final GlobalBlockQueue blockQueue;

    @Inject public BukkitRegionManager(@Nonnull WorldUtil worldUtil, @Nonnull GlobalBlockQueue blockQueue) {
        super(worldUtil, blockQueue);
        this.blockQueue = blockQueue;
    }

    @Override public boolean handleClear(Plot plot, Runnable whenDone, PlotManager manager) {
        return false;
    }

    @Override public int[] countEntities(Plot plot) {
        int[] existing = (int[]) plot.getMeta("EntityCount");
        if (existing != null && (System.currentTimeMillis() - (long) plot.getMeta("EntityCountTime")
            < 1000)) {
            return existing;
        }
        PlotArea area = plot.getArea();
        World world = BukkitUtil.getWorld(area.getWorldName());
        Location bot = plot.getBottomAbs();
        Location top = plot.getTopAbs();
        int bx = bot.getX() >> 4;
        int bz = bot.getZ() >> 4;

        int tx = top.getX() >> 4;
        int tz = top.getZ() >> 4;

        int size = tx - bx << 4;

        Set<Chunk> chunks = new HashSet<>();
        for (int X = bx; X <= tx; X++) {
            for (int Z = bz; Z <= tz; Z++) {
                if (world.isChunkLoaded(X, Z)) {
                    chunks.add(world.getChunkAt(X, Z));
                }
            }
        }

        boolean doWhole = false;
        List<Entity> entities = null;
        if (size > 200 && chunks.size() > 200) {
            entities = world.getEntities();
            if (entities.size() < 16 + size / 8) {
                doWhole = true;
            }
        }

        int[] count = new int[6];
        if (doWhole) {
            for (Entity entity : entities) {
                org.bukkit.Location location = entity.getLocation();
                PaperLib.getChunkAtAsync(location).thenAccept(chunk -> {
                    if (chunks.contains(chunk)) {
                        int X = chunk.getX();
                        int Z = chunk.getZ();
                        if (X > bx && X < tx && Z > bz && Z < tz) {
                            count(count, entity);
                        } else {
                            Plot other = area.getPlot(BukkitUtil.adapt(location));
                            if (plot.equals(other)) {
                                count(count, entity);
                            }
                        }
                    }
                });
            }
        } else {
            for (Chunk chunk : chunks) {
                int X = chunk.getX();
                int Z = chunk.getZ();
                Entity[] entities1 = chunk.getEntities();
                for (Entity entity : entities1) {
                    if (X == bx || X == tx || Z == bz || Z == tz) {
                        Plot other = area.getPlot(BukkitUtil.adapt(entity.getLocation()));
                        if (plot.equals(other)) {
                            count(count, entity);
                        }
                    } else {
                        count(count, entity);
                    }
                }
            }
        }
        return count;
    }

    @Override public boolean regenerateRegion(final Location pos1, final Location pos2,
        final boolean ignoreAugment, final Runnable whenDone) {
        final BukkitWorld world = new BukkitWorld((World) pos1.getWorld());

        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;

        final QueueCoordinator queue = blockQueue.getNewQueue(world);
        queue.addReadChunks(new CuboidRegion(pos1.getBlockVector3(), pos2.getBlockVector3()).getChunks());
        queue.setChunkConsumer(chunk -> {

            int x = chunk.getX();
            int z = chunk.getZ();
            int xxb = x << 4;
            int zzb = z << 4;
            int xxt = xxb + 15;
            int zzt = zzb + 15;
            if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z) {
                AugmentedUtils
                    .bypass(ignoreAugment, () -> queue.regenChunk(chunk.getX(), chunk.getZ()));
                return;
            }
            boolean checkX1 = false;

            int xxb2;

            if (x == bcx) {
                xxb2 = p1x - 1;
                checkX1 = true;
            } else {
                xxb2 = xxb;
            }
            boolean checkX2 = false;
            int xxt2;
            if (x == tcx) {
                xxt2 = p2x + 1;
                checkX2 = true;
            } else {
                xxt2 = xxt;
            }
            boolean checkZ1 = false;
            int zzb2;
            if (z == bcz) {
                zzb2 = p1z - 1;
                checkZ1 = true;
            } else {
                zzb2 = zzb;
            }
            boolean checkZ2 = false;
            int zzt2;
            if (z == tcz) {
                zzt2 = p2z + 1;
                checkZ2 = true;
            } else {
                zzt2 = zzt;
            }
            final ContentMap map = new ContentMap();
            if (checkX1) {
                map.saveRegion(world, xxb, xxb2, zzb2, zzt2); //
            }
            if (checkX2) {
                map.saveRegion(world, xxt2, xxt, zzb2, zzt2); //
            }
            if (checkZ1) {
                map.saveRegion(world, xxb2, xxt2, zzb, zzb2); //
            }
            if (checkZ2) {
                map.saveRegion(world, xxb2, xxt2, zzt2, zzt); //
            }
            if (checkX1 && checkZ1) {
                map.saveRegion(world, xxb, xxb2, zzb, zzb2); //
            }
            if (checkX2 && checkZ1) {
                map.saveRegion(world, xxt2, xxt, zzb, zzb2); // ?
            }
            if (checkX1 && checkZ2) {
                map.saveRegion(world, xxb, xxb2, zzt2, zzt); // ?
            }
            if (checkX2 && checkZ2) {
                map.saveRegion(world, xxt2, xxt, zzt2, zzt); //
            }
            CuboidRegion currentPlotClear =
                RegionUtil.createRegion(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
            map.saveEntitiesOut(Bukkit.getWorld(world.getName()).getChunkAt(x, z),
                currentPlotClear);
            AugmentedUtils.bypass(ignoreAugment, () -> ChunkManager
                .setChunkInPlotArea(null, new RunnableVal<ScopedQueueCoordinator>() {
                    @Override public void run(ScopedQueueCoordinator value) {
                        Location min = value.getMin();
                        int bx = min.getX();
                        int bz = min.getZ();
                        for (int x1 = 0; x1 < 16; x1++) {
                            for (int z1 = 0; z1 < 16; z1++) {
                                PlotLoc plotLoc = new PlotLoc(bx + x1, bz + z1);
                                BaseBlock[] ids = map.allBlocks.get(plotLoc);
                                if (ids != null) {
                                    for (int y = 0; y < Math.min(128, ids.length); y++) {
                                        BaseBlock id = ids[y];
                                        if (id != null) {
                                            value.setBlock(x1, y, z1, id);
                                        } else {
                                            value.setBlock(x1, y, z1,
                                                BlockTypes.AIR.getDefaultState());
                                        }
                                    }
                                    for (int y = Math.min(128, ids.length); y < ids.length; y++) {
                                        BaseBlock id = ids[y];
                                        if (id != null) {
                                            value.setBlock(x1, y, z1, id);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, world.getName(), chunk));
            //map.restoreBlocks(worldObj, 0, 0);
            map.restoreEntities(Bukkit.getWorld(world.getName()), 0, 0);
        });
        queue.setCompleteTask(whenDone);
        queue.enqueue();
        return true;
    }

    @Override public void clearAllEntities(Location pos1, Location pos2) {
        String world = pos1.getWorldName();

        final World bukkitWorld = BukkitUtil.getWorld(world);
        final List<Entity> entities;
        if (bukkitWorld != null) {
            entities = new ArrayList<>(bukkitWorld.getEntities());
        } else {
            entities = new ArrayList<>();
        }

        int bx = pos1.getX();
        int bz = pos1.getZ();
        int tx = pos2.getX();
        int tz = pos2.getZ();
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) {
                org.bukkit.Location location = entity.getLocation();
                if (location.getX() >= bx && location.getX() <= tx && location.getZ() >= bz
                    && location.getZ() <= tz) {
                    if (entity.hasMetadata("ps-tmp-teleport")) {
                        continue;
                    }
                    entity.remove();
                }
            }
        }
    }

    private void count(int[] count, Entity entity) {
        final com.sk89q.worldedit.world.entity.EntityType entityType =
            BukkitAdapter.adapt(entity.getType());

        if (EntityCategories.PLAYER.contains(entityType)) {
            return;
        } else if (EntityCategories.PROJECTILE.contains(entityType) || EntityCategories.OTHER
            .contains(entityType) || EntityCategories.HANGING.contains(entityType)) {
            count[CAP_MISC]++;
        } else if (EntityCategories.ANIMAL.contains(entityType) || EntityCategories.VILLAGER
            .contains(entityType) || EntityCategories.TAMEABLE.contains(entityType)) {
            count[CAP_MOB]++;
            count[CAP_ANIMAL]++;
        } else if (EntityCategories.VEHICLE.contains(entityType)) {
            count[CAP_VEHICLE]++;
        } else if (EntityCategories.HOSTILE.contains(entityType)) {
            count[CAP_MOB]++;
            count[CAP_MONSTER]++;
        }
        count[CAP_ENTITY]++;
    }
}
