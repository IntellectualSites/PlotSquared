/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContentMap {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ContentMap.class.getSimpleName());

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
                saveBlocks(world, x, z);
            }
        }
    }

    void saveEntitiesOut(Chunk chunk, CuboidRegion region) {
        for (Entity entity : chunk.getEntities()) {
            Location location = BukkitUtil.adapt(entity.getLocation());
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

    void saveEntitiesIn(Chunk chunk, CuboidRegion region, boolean delete) {
        for (Entity entity : chunk.getEntities()) {
            Location location = BukkitUtil.adapt(entity.getLocation());
            int x = location.getX();
            int z = location.getZ();
            if (!BukkitChunkManager.isIn(region, x, z)) {
                continue;
            }
            if (entity.getVehicle() != null) {
                continue;
            }
            EntityWrapper wrap = new ReplicatingEntityWrapper(entity, (short) 2);
            wrap.saveEntity();
            this.entities.add(wrap);
            if (delete) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
    }

    void restoreEntities(World world) {
        for (EntityWrapper entity : this.entities) {
            try {
                entity.spawn(world, 0, 0);
            } catch (Exception e) {
                LOGGER.error("Failed to restore entity", e);
            }
        }
        this.entities.clear();
    }

    private void saveBlocks(BukkitWorld world, int x, int z) {
        BaseBlock[] ids = new BaseBlock[world.getMaxY() - world.getMinY() + 1];
        for (short yIndex = 0; yIndex <= world.getMaxY() - world.getMinY(); yIndex++) {
            BaseBlock block = world.getFullBlock(BlockVector3.at(x, yIndex + world.getMinY(), z));
            ids[yIndex] = block;
        }
        PlotLoc loc = new PlotLoc(x, z);
        this.allBlocks.put(loc, ids);
    }

}
