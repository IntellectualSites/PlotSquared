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

import com.google.inject.Singleton;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.listener.WEExtent;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BaseBlock;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;

import static com.plotsquared.core.util.entity.EntityCategories.CAP_ANIMAL;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ENTITY;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MISC;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MOB;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MONSTER;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_VEHICLE;

@Singleton
public class BukkitChunkManager extends ChunkManager {

    public static boolean isIn(CuboidRegion region, int x, int z) {
        return x >= region.getMinimumPoint().getX() && x <= region.getMaximumPoint().getX()
            && z >= region.getMinimumPoint().getZ() && z <= region.getMaximumPoint().getZ();
    }

    public static ContentMap swapChunk(World world1, World world2, Chunk pos1, Chunk pos2,
        CuboidRegion r1, CuboidRegion r2) {
        ContentMap map = new ContentMap();
        int relX = r2.getMinimumPoint().getX() - r1.getMinimumPoint().getX();
        int relZ = r2.getMinimumPoint().getZ() - r1.getMinimumPoint().getZ();

        map.saveEntitiesIn(pos1, r1, relX, relZ, true);
        map.saveEntitiesIn(pos2, r2, -relX, -relZ, true);

        int sx = pos1.getX() << 4;
        int sz = pos1.getZ() << 4;

        BukkitWorld bukkitWorld1 = new BukkitWorld(world1);
        BukkitWorld bukkitWorld2 = new BukkitWorld(world2);

        QueueCoordinator queue1 =
            PlotSquared.platform().getGlobalBlockQueue().getNewQueue(bukkitWorld1);
        QueueCoordinator queue2 =
            PlotSquared.platform().getGlobalBlockQueue().getNewQueue(bukkitWorld2);

        for (int x = Math.max(r1.getMinimumPoint().getX(), sx);
             x <= Math.min(r1.getMaximumPoint().getX(), sx + 15); x++) {
            for (int z = Math.max(r1.getMinimumPoint().getZ(), sz);
                 z <= Math.min(r1.getMaximumPoint().getZ(), sz + 15); z++) {
                for (int y = 0; y < 256; y++) {
                    Block block1 = world1.getBlockAt(x, y, z);
                    BaseBlock baseBlock1 = bukkitWorld1.getFullBlock(BlockVector3.at(x, y, z));
                    BlockData data1 = block1.getBlockData();

                    int xx = x + relX;
                    int zz = z + relZ;

                    Block block2 = world2.getBlockAt(xx, y, zz);
                    BaseBlock baseBlock2 = bukkitWorld2.getFullBlock(BlockVector3.at(xx, y, zz));
                    BlockData data2 = block2.getBlockData();

                    if (block1.isEmpty()) {
                        if (!block2.isEmpty()) {
                            queue1.setBlock(x, y, z, baseBlock2);
                            queue2.setBlock(xx, y, zz, WEExtent.AIRBASE);
                        }
                    } else if (block2.isEmpty()) {
                        queue1.setBlock(x, y, z, WEExtent.AIRBASE);
                        queue2.setBlock(xx, y, zz, baseBlock1);
                    } else if (block1.equals(block2)) {
                        if (!data1.matches(data2)) {
                            block1.setBlockData(data2);
                            block2.setBlockData(data1);
                        }
                    } else {
                        queue1.setBlock(x, y, z, baseBlock2);
                        queue2.setBlock(xx, y, zz, baseBlock1);
                    }
                }
            }
        }
        queue1.enqueue();
        queue2.enqueue();
        return map;
    }

    @Override
    public CompletableFuture<?> loadChunk(String world, BlockVector2 chunkLoc, boolean force) {
        return PaperLib
            .getChunkAtAsync(BukkitUtil.getWorld(world), chunkLoc.getX(), chunkLoc.getZ(), force);
    }

}
