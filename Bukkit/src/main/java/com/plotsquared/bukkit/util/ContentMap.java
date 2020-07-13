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

import com.plotsquared.bukkit.entity.EntityWrapper;
import com.plotsquared.bukkit.entity.ReplicatingEntityWrapper;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.PlotLoc;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContentMap {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + ContentMap.class.getSimpleName());

    final Set<EntityWrapper> entities;
    final Map<PlotLoc, BaseBlock[]> allBlocks;

    ContentMap() {
        this.entities = new HashSet<>();
        this.allBlocks = new HashMap<>();
    }

    public void saveRegion(BukkitWorld world, int x1, int x2, int z1, int z2) {
        if (z1 > z2) {
            int tmp = z1;
            z1 = z2;
            z2 = tmp;
        }
        if (x1 > x2) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                saveBlocks(world, 256, x, z, 0, 0);
            }
        }
    }

    void saveEntitiesOut(Chunk chunk, CuboidRegion region) {
        for (Entity entity : chunk.getEntities()) {
            Location location = BukkitUtil.getLocation(entity);
            int x = location.getX();
            int z = location.getZ();
            if (BukkitChunkManager.isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new ReplicatingEntityWrapper(entity, (short) 2);
            wrap.saveEntity();
            this.entities.add(wrap);
        }
    }

    void saveEntitiesIn(Chunk chunk, CuboidRegion region) {
        saveEntitiesIn(chunk, region, 0, 0, false);
    }

    void saveEntitiesIn(Chunk chunk, CuboidRegion region, int offsetX, int offsetZ,
        boolean delete) {
        for (Entity entity : chunk.getEntities()) {
            Location location = BukkitUtil.getLocation(entity);
            int x = location.getX();
            int z = location.getZ();
            if (!BukkitChunkManager.isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new ReplicatingEntityWrapper(entity, (short) 2);
            wrap.x += offsetX;
            wrap.z += offsetZ;
            wrap.saveEntity();
            this.entities.add(wrap);
            if (delete) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    void restoreEntities(World world, int xOffset, int zOffset) {
        for (EntityWrapper entity : this.entities) {
            try {
                entity.spawn(world, xOffset, zOffset);
            } catch (Exception e) {
                logger.error("Failed to restore entity", e);
            }
        }
        this.entities.clear();
    }

    //todo optimize maxY
    void saveBlocks(BukkitWorld world, int maxY, int x, int z, int offsetX, int offsetZ) {
        maxY = Math.min(255, maxY);
        BaseBlock[] ids = new BaseBlock[maxY + 1];
        for (short y = 0; y <= maxY; y++) {
            BaseBlock block = world.getFullBlock(BlockVector3.at(x, y, z));
            ids[y] = block;
        }
        PlotLoc loc = new PlotLoc(x + offsetX, z + offsetZ);
        this.allBlocks.put(loc, ids);
    }
}
