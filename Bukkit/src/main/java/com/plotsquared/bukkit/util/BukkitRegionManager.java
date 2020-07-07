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

import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.AugmentedUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.PlotLoc;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ANIMAL;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ENTITY;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MISC;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MOB;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MONSTER;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_VEHICLE;

public class BukkitRegionManager extends RegionManager {

    public static boolean isIn(CuboidRegion region, int x, int z) {
        return x >= region.getMinimumPoint().getX() && x <= region.getMaximumPoint().getX()
            && z >= region.getMinimumPoint().getZ() && z <= region.getMaximumPoint().getZ();
    }

    @Override public Set<BlockVector2> getChunkChunks(String world) {
        Set<BlockVector2> chunks = super.getChunkChunks(world);
        if (Bukkit.isPrimaryThread()) {
            for (Chunk chunk : Objects.requireNonNull(Bukkit.getWorld(world)).getLoadedChunks()) {
                BlockVector2 loc = BlockVector2.at(chunk.getX() >> 5, chunk.getZ() >> 5);
                chunks.add(loc);
            }
        } else {
            final Semaphore semaphore = new Semaphore(1);
            try {
                PlotSquared.debug("Attempting to make an asynchronous call to getLoadedChunks."
                    + " Will halt the calling thread until completed.");
                semaphore.acquire();
                Bukkit.getScheduler().runTask(BukkitPlatform.getPlugin(BukkitPlatform.class), () -> {
                    for (Chunk chunk : Objects.requireNonNull(Bukkit.getWorld(world))
                        .getLoadedChunks()) {
                        BlockVector2 loc = BlockVector2.at(chunk.getX() >> 5, chunk.getZ() >> 5);
                        chunks.add(loc);
                    }
                    semaphore.release();
                });
                semaphore.acquireUninterruptibly();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return chunks;
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
                            Plot other = area.getPlot(BukkitUtil.getLocation(location));
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
                        Plot other = area.getPlot(BukkitUtil.getLocation(entity));
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

    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos,
        final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();

        final CuboidRegion region =
            RegionUtil.createRegion(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
        final World oldWorld = Bukkit.getWorld(pos1.getWorld());
        final BukkitWorld oldBukkitWorld = new BukkitWorld(oldWorld);
        final World newWorld = Bukkit.getWorld(newPos.getWorld());
        assert newWorld != null;
        assert oldWorld != null;
        final String newWorldName = newWorld.getName();
        final ContentMap map = new ContentMap();
        final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(newWorldName, false);
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                int bx = value[2];
                int bz = value[3];
                int tx = value[4];
                int tz = value[5];
                BlockVector2 loc = BlockVector2.at(value[0], value[1]);
                int cxx = loc.getX() << 4;
                int czz = loc.getZ() << 4;
                PaperLib.getChunkAtAsync(oldWorld, loc.getX(), loc.getZ())
                    .thenAccept(chunk1 -> map.saveEntitiesIn(chunk1, region)).thenRun(() -> {
                    for (int x = bx & 15; x <= (tx & 15); x++) {
                        for (int z = bz & 15; z <= (tz & 15); z++) {
                            map.saveBlocks(oldBukkitWorld, 256, cxx + x, czz + z, relX, relZ);
                        }
                    }
                });
            }
        }, () -> {
            for (Entry<PlotLoc, BaseBlock[]> entry : map.allBlocks.entrySet()) {
                PlotLoc loc = entry.getKey();
                BaseBlock[] blocks = entry.getValue();
                for (int y = 0; y < blocks.length; y++) {
                    if (blocks[y] != null) {
                        BaseBlock block = blocks[y];
                        queue.setBlock(loc.getX(), y, loc.getZ(), block);
                    }
                }
            }
            queue.enqueue();
            GlobalBlockQueue.IMP.addEmptyTask(() -> {
                //map.restoreBlocks(newWorld, 0, 0);
                map.restoreEntities(newWorld, relX, relZ);
                TaskManager.runTask(whenDone);
            });
        }, 5);
        return true;
    }

    @Override
    public boolean regenerateRegion(final Location pos1, final Location pos2,
        final boolean ignoreAugment, final Runnable whenDone) {
        final String world = pos1.getWorld();

        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;

        final List<BlockVector2> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(BlockVector2.at(x, z));
            }
        }
        final World worldObj = Bukkit.getWorld(world);
        checkNotNull(worldObj, "Critical error during regeneration.");
        final BukkitWorld bukkitWorldObj = new BukkitWorld(worldObj);
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && System.currentTimeMillis() - start < 5) {
                    final BlockVector2 chunk = chunks.remove(0);
                    int x = chunk.getX();
                    int z = chunk.getZ();
                    int xxb = x << 4;
                    int zzb = z << 4;
                    int xxt = xxb + 15;
                    int zzt = zzb + 15;
                    PaperLib.getChunkAtAsync(worldObj, x, z, false).thenAccept(chunkObj -> {
                        if (chunkObj == null) {
                            return;
                        }
                        final LocalBlockQueue queue =
                            GlobalBlockQueue.IMP.getNewQueue(world, false);
                        if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z) {
                            AugmentedUtils.bypass(ignoreAugment,
                                () -> queue.regenChunkSafe(chunk.getX(), chunk.getZ()));
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
                            map.saveRegion(bukkitWorldObj, xxb, xxb2, zzb2, zzt2); //
                        }
                        if (checkX2) {
                            map.saveRegion(bukkitWorldObj, xxt2, xxt, zzb2, zzt2); //
                        }
                        if (checkZ1) {
                            map.saveRegion(bukkitWorldObj, xxb2, xxt2, zzb, zzb2); //
                        }
                        if (checkZ2) {
                            map.saveRegion(bukkitWorldObj, xxb2, xxt2, zzt2, zzt); //
                        }
                        if (checkX1 && checkZ1) {
                            map.saveRegion(bukkitWorldObj, xxb, xxb2, zzb, zzb2); //
                        }
                        if (checkX2 && checkZ1) {
                            map.saveRegion(bukkitWorldObj, xxt2, xxt, zzb, zzb2); // ?
                        }
                        if (checkX1 && checkZ2) {
                            map.saveRegion(bukkitWorldObj, xxb, xxb2, zzt2, zzt); // ?
                        }
                        if (checkX2 && checkZ2) {
                            map.saveRegion(bukkitWorldObj, xxt2, xxt, zzt2, zzt); //
                        }
                        CuboidRegion currentPlotClear = RegionUtil
                            .createRegion(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ());
                        map.saveEntitiesOut(chunkObj, currentPlotClear);
                        AugmentedUtils.bypass(ignoreAugment, () -> ChunkManager
                            .setChunkInPlotArea(null, new RunnableVal<ScopedLocalBlockQueue>() {
                                @Override public void run(ScopedLocalBlockQueue value) {
                                    Location min = value.getMin();
                                    int bx = min.getX();
                                    int bz = min.getZ();
                                    for (int x1 = 0; x1 < 16; x1++) {
                                        for (int z1 = 0; z1 < 16; z1++) {
                                            PlotLoc plotLoc = new PlotLoc(bx + x1, bz + z1);
                                            BaseBlock[] ids = map.allBlocks.get(plotLoc);
                                            if (ids != null) {
                                                for (int y = 0;
                                                     y < Math.min(128, ids.length); y++) {
                                                    BaseBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x1, y, z1, id);
                                                    } else {
                                                        value.setBlock(x1, y, z1,
                                                            BlockTypes.AIR.getDefaultState());
                                                    }
                                                }
                                                for (int y = Math.min(128, ids.length);
                                                     y < ids.length; y++) {
                                                    BaseBlock id = ids[y];
                                                    if (id != null) {
                                                        value.setBlock(x1, y, z1, id);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, world, chunk));
                        //map.restoreBlocks(worldObj, 0, 0);
                        map.restoreEntities(worldObj, 0, 0);
                    });
                }
                if (!chunks.isEmpty()) {
                    TaskManager.runTaskLater(this, 1);
                } else {
                    TaskManager.runTaskLater(whenDone, 1);
                }
            }
        });
        return true;
    }

    @Override public void clearAllEntities(Location pos1, Location pos2) {
        String world = pos1.getWorld();
        List<Entity> entities = BukkitUtil.getEntities(world);
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

    @Override
    public void swap(Location bot1, Location top1, Location bot2, Location top2,
        final Runnable whenDone) {
        CuboidRegion region1 =
            RegionUtil.createRegion(bot1.getX(), top1.getX(), bot1.getZ(), top1.getZ());
        CuboidRegion region2 =
            RegionUtil.createRegion(bot2.getX(), top2.getX(), bot2.getZ(), top2.getZ());
        final World world1 = Bukkit.getWorld(bot1.getWorld());
        final World world2 = Bukkit.getWorld(bot2.getWorld());
        checkNotNull(world1, "Critical error during swap.");
        checkNotNull(world2, "Critical error during swap.");
        int relX = bot2.getX() - bot1.getX();
        int relZ = bot2.getZ() - bot1.getZ();

        final ArrayDeque<ContentMap> maps = new ArrayDeque<>();

        for (int x = bot1.getX() >> 4; x <= top1.getX() >> 4; x++) {
            for (int z = bot1.getZ() >> 4; z <= top1.getZ() >> 4; z++) {
                Chunk chunk1 = world1.getChunkAt(x, z);
                Chunk chunk2 = world2.getChunkAt(x + (relX >> 4), z + (relZ >> 4));
                maps.add(
                    BukkitChunkManager.swapChunk(world1, world2, chunk1, chunk2, region1, region2));
            }
        }
        GlobalBlockQueue.IMP.addEmptyTask(() -> {
            for (ContentMap map : maps) {
                map.restoreEntities(world1, 0, 0);
                TaskManager.runTaskLater(whenDone, 1);
            }
        });
    }

    @Override
    public void setBiome(final CuboidRegion region, final int extendBiome, final BiomeType biome,
        final String world, final Runnable whenDone) {
        Location pos1 = new Location(world, region.getMinimumPoint().getX() - extendBiome,
            region.getMinimumPoint().getY(), region.getMinimumPoint().getZ() - extendBiome);
        Location pos2 = new Location(world, region.getMaximumPoint().getX() + extendBiome,
            region.getMaximumPoint().getY(), region.getMaximumPoint().getZ() + extendBiome);
        final LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);

        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                BlockVector2 loc = BlockVector2.at(value[0], value[1]);
                ChunkManager.manager.loadChunk(world, loc, false).thenRun(() -> {
                    MainUtil.setBiome(world, value[2], value[3], value[4], value[5], biome);
                    queue.refreshChunk(value[0], value[1]);
                });
            }
        }, whenDone, 5);
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
