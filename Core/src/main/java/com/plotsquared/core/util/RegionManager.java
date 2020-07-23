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
package com.plotsquared.core.util;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.BasicQueueCoordinator;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class RegionManager {

    private static final Logger logger =
        LoggerFactory.getLogger("P2/" + RegionManager.class.getSimpleName());

    public static RegionManager manager = null;
    @Inject private WorldUtil worldUtil;
    @Inject private ChunkManager chunkManager;
    @Inject private GlobalBlockQueue blockQueue;

    public static BlockVector2 getRegion(Location location) {
        int x = location.getX() >> 9;
        int z = location.getZ() >> 9;
        return BlockVector2.at(x, z);
    }

    /**
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     */
    public abstract int[] countEntities(Plot plot);

    public Set<BlockVector2> getChunkChunks(String world) {
        File folder =
            new File(PlotSquared.platform().getWorldContainer(), world + File.separator + "region");
        File[] regionFiles = folder.listFiles();
        if (regionFiles == null) {
            throw new RuntimeException(
                "Could not find worlds folder: " + folder + " ? (no read access?)");
        }
        HashSet<BlockVector2> chunks = new HashSet<>();
        for (File file : regionFiles) {
            String name = file.getName();
            if (name.endsWith("mca")) {
                String[] split = name.split("\\.");
                try {
                    int x = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    BlockVector2 loc = BlockVector2.at(x, z);
                    chunks.add(loc);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return chunks;
    }

    public void deleteRegionFiles(final String world, final Collection<BlockVector2> chunks,
        final Runnable whenDone) {
        TaskManager.runTaskAsync(() -> {
            for (BlockVector2 loc : chunks) {
                String directory =
                    world + File.separator + "region" + File.separator + "r." + loc.getX() + "."
                        + loc.getZ() + ".mca";
                File file = new File(PlotSquared.platform().getWorldContainer(), directory);
                logger.info("[P2] - Deleting file: {} (max 1024 chunks)", file.getName());
                if (file.exists()) {
                    file.delete();
                }
            }
            TaskManager.runTask(whenDone);
        });
    }

    public boolean setCuboids(final PlotArea area, final Set<CuboidRegion> regions,
        final Pattern blocks, int minY, int maxY, @Nullable QueueCoordinator queue) {
        boolean enqueue = false;
        if (queue == null) {
            queue = area.getQueue();
            enqueue = true;
        }
        for (CuboidRegion region : regions) {
            Location pos1 = Location.at(area.getWorldName(), region.getMinimumPoint().getX(), minY,
                region.getMinimumPoint().getZ());
            Location pos2 = Location.at(area.getWorldName(), region.getMaximumPoint().getX(), maxY,
                region.getMaximumPoint().getZ());
            queue.setCuboid(pos1, pos2, blocks);
        }
        return !enqueue || queue.enqueue();
    }

    /**
     * Notify any plugins that may want to modify clear behaviour that a clear is occuring
     *
     * @return true if the notified will accept the clear task
     */
    public boolean notifyClear(PlotManager manager) {
        return false;
    }

    /**
     * Only called when {@link RegionManager#notifyClear(PlotManager)} returns true in specific PlotManagers
     *
     * @return true if the clear worked. False if someone went wrong so P2 can then handle the clear
     */
    public abstract boolean handleClear(Plot plot, final Runnable whenDone, PlotManager manager);

    /**
     * Copy a region to a new location (in the same world)
     */
    public boolean copyRegion(final Location pos1, final Location pos2, final Location newPos,
        final Runnable whenDone) {
        final int relX = newPos.getX() - pos1.getX();
        final int relZ = newPos.getZ() - pos1.getZ();
        final com.sk89q.worldedit.world.World oldWorld = worldUtil.getWeWorld(pos1.getWorldName());
        final com.sk89q.worldedit.world.World newWorld =
            worldUtil.getWeWorld(newPos.getWorldName());
        final QueueCoordinator copyFrom = blockQueue.getNewQueue(oldWorld);
        final BasicQueueCoordinator copyTo =
            (BasicQueueCoordinator) blockQueue.getNewQueue(newWorld);
        copyFromTo(pos1, pos2, relX, relZ, oldWorld, copyFrom, copyTo, false);
        copyFrom.setCompleteTask(copyTo::enqueue);
        copyFrom.setReadRegion(new CuboidRegion(BlockVector3.at(pos1.getX(), 0, pos1.getZ()),
            BlockVector3.at(pos2.getX(), 0, pos2.getZ())));
        copyTo.setCompleteTask(whenDone);
        copyFrom.enqueue();
        return true;
    }

    /**
     * Assumptions:<br>
     * - pos1 and pos2 are in the same plot<br>
     * It can be harmful to the world if parameters outside this scope are provided
     */
    public abstract boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment,
        Runnable whenDone);

    public abstract void clearAllEntities(Location pos1, Location pos2);

    public void swap(Location pos1, Location pos2, Location swapPos, final Runnable whenDone) {
        int relX = swapPos.getX() - pos1.getX();
        int relZ = swapPos.getZ() - pos1.getZ();

        World world1 = worldUtil.getWeWorld(pos1.getWorldName());
        World world2 = worldUtil.getWeWorld(swapPos.getWorldName());

        QueueCoordinator fromQueue1 = blockQueue.getNewQueue(world1);
        QueueCoordinator fromQueue2 = blockQueue.getNewQueue(world2);
        fromQueue1.setUnloadAfter(false);
        fromQueue2.setUnloadAfter(false);
        fromQueue1.setReadRegion(new CuboidRegion(pos1.getBlockVector3(), pos2.getBlockVector3()));
        fromQueue2.setReadRegion(new CuboidRegion(swapPos.getBlockVector3(), BlockVector3
            .at(swapPos.getX() + pos2.getX() - pos1.getX(), 0,
                swapPos.getZ() + pos2.getZ() - pos1.getZ())));
        QueueCoordinator toQueue1 = blockQueue.getNewQueue(world1);
        QueueCoordinator toQueue2 = blockQueue.getNewQueue(world2);

        copyFromTo(pos1, pos2, relX, relZ, world1, fromQueue1, toQueue2, true);
        copyFromTo(pos1, pos2, relX, relZ, world1, fromQueue2, toQueue1, true);
        fromQueue1.setCompleteTask(fromQueue2::enqueue);
        fromQueue2.setCompleteTask(toQueue1::enqueue);
        toQueue1.setCompleteTask(toQueue2::enqueue);
        toQueue2.setCompleteTask(whenDone);
    }

    private void copyFromTo(Location pos1, Location pos2, int relX, int relZ, World world1,
        QueueCoordinator fromQueue, QueueCoordinator toQueue, boolean removeEntities) {
        fromQueue.setChunkConsumer(chunk -> {
            int cx = chunk.getX();
            int cz = chunk.getZ();
            int cbx = cx << 4;
            int cbz = cz << 4;
            int bx = Math.max(pos1.getX() & 15, 0);
            int bz = Math.max(pos1.getZ() & 15, 0);
            int tx = Math.min(pos2.getX() & 15, 15);
            int tz = Math.min(pos2.getZ() & 15, 15);
            for (int y = 0; y < 256; y++) {
                for (int x = bx; x <= tx; x++) {
                    for (int z = bz; z <= tz; z++) {
                        int rx = cbx + x;
                        int rz = cbz + z;
                        BlockVector3 loc = BlockVector3.at(rx, y, rz);
                        toQueue.setBlock(rx + relX, y, rz + relZ, world1.getFullBlock(loc));
                        toQueue.setBiome(rx + relX, y, rz + relZ, world1.getBiome(loc));
                    }
                }
            }
            Region region = new CuboidRegion(BlockVector3.at(cbx + bx, 0, cbz + bz),
                BlockVector3.at(cbx + tx, 255, cbz + tz));
            toQueue.addEntities(world1.getEntities(region));
            if (removeEntities) {
                for (Entity entity : world1.getEntities(region)) {
                    entity.remove();
                }
            }
        });
    }

    public void setBiome(final CuboidRegion region, final int extendBiome, final BiomeType biome,
        final String world, final Runnable whenDone) {
        Location pos1 = Location.at(world, region.getMinimumPoint().getX() - extendBiome,
            region.getMinimumPoint().getY(), region.getMinimumPoint().getZ() - extendBiome);
        Location pos2 = Location.at(world, region.getMaximumPoint().getX() + extendBiome,
            region.getMaximumPoint().getY(), region.getMaximumPoint().getZ() + extendBiome);
        final QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world));

        final int minX = pos1.getX();
        final int minZ = pos1.getZ();
        final int maxX = pos2.getX();
        final int maxZ = pos2.getZ();
        queue.setReadRegion(region);
        queue.setChunkConsumer(blockVector2 -> {
            final int cx = blockVector2.getX() << 4;
            final int cz = blockVector2.getZ() << 4;
            WorldUtil
                .setBiome(world, Math.max(minX, cx), Math.max(minZ, cz), Math.min(maxX, cx + 15),
                    Math.min(maxZ, cz + 15), biome);
            worldUtil.refreshChunk(blockVector2.getBlockX(), blockVector2.getBlockZ(), world);
        });
        queue.setCompleteTask(whenDone);
        queue.enqueue();
    }
}
