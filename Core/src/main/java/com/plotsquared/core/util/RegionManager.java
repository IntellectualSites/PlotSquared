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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class RegionManager {

    private static final Logger logger =
        LoggerFactory.getLogger("P2/" + RegionManager.class.getSimpleName());

    public static RegionManager manager = null;
    private final ChunkManager chunkManager;

    public RegionManager(@Nonnull final ChunkManager chunkManager, @Nonnull WorldUtil worldUtil) {
        this.chunkManager = chunkManager;
    }

    public static BlockVector2 getRegion(Location location) {
        int x = location.getX() >> 9;
        int z = location.getZ() >> 9;
        return BlockVector2.at(x, z);
    }

    public void largeRegionTask(final String world, final CuboidRegion region,
        final RunnableVal<BlockVector2> task, final Runnable whenDone) {
        TaskManager.runTaskAsync(() -> {
            HashSet<BlockVector2> chunks = new HashSet<>();
            Set<BlockVector2> mcrs = this.getChunkChunks(world);
            for (BlockVector2 mcr : mcrs) {
                int bx = mcr.getX() << 9;
                int bz = mcr.getZ() << 9;
                int tx = bx + 511;
                int tz = bz + 511;
                if (bx <= region.getMaximumPoint().getX() && tx >= region.getMinimumPoint().getX()
                    && bz <= region.getMaximumPoint().getZ() && tz >= region.getMinimumPoint()
                    .getZ()) {
                    for (int x = bx >> 4; x <= (tx >> 4); x++) {
                        int cbx = x << 4;
                        int ctx = cbx + 15;
                        if (cbx <= region.getMaximumPoint().getX() && ctx >= region
                            .getMinimumPoint().getX()) {
                            for (int z = bz >> 4; z <= (tz >> 4); z++) {
                                int cbz = z << 4;
                                int ctz = cbz + 15;
                                if (cbz <= region.getMaximumPoint().getZ() && ctz >= region
                                    .getMinimumPoint().getZ()) {
                                    chunks.add(BlockVector2.at(x, z));
                                }
                            }
                        }
                    }
                }
            }
            TaskManager.getPlatformImplementation().objectTask(chunks, new RunnableVal<BlockVector2>() {
                @Override public void run(BlockVector2 value) {
                    chunkManager.loadChunk(world, value, false).thenRun(() -> task.run(value));
                }
            }).thenAccept(ignore ->
                TaskManager.getPlatformImplementation().taskLater(whenDone, TaskTime.ticks(1L)));
        });
    }

    /**
     * 0 = Entity
     * 1 = Animal
     * 2 = Monster
     * 3 = Mob
     * 4 = Boat
     * 5 = Misc
     *
     * @param plot
     * @return
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

    public void deleteRegionFiles(String world, Collection<BlockVector2> chunks) {
        deleteRegionFiles(world, chunks, null);
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
        final Pattern blocks, int minY, int maxY) {
        QueueCoordinator queue = area.getQueue(false);
        for (CuboidRegion region : regions) {
            Location pos1 = Location.at(area.getWorldName(), region.getMinimumPoint().getX(), minY,
                region.getMinimumPoint().getZ());
            Location pos2 = Location.at(area.getWorldName(), region.getMaximumPoint().getX(), maxY,
                region.getMaximumPoint().getZ());
            queue.setCuboid(pos1, pos2, blocks);
        }
        return queue.enqueue();
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
    public abstract boolean copyRegion(Location pos1, Location pos2, Location newPos,
        Runnable whenDone);

    /**
     * Assumptions:<br>
     * - pos1 and pos2 are in the same plot<br>
     * It can be harmful to the world if parameters outside this scope are provided
     *
     * @param pos1
     * @param pos2
     * @param whenDone
     * @return
     */
    public abstract boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment,
        Runnable whenDone);

    public abstract void clearAllEntities(Location pos1, Location pos2);

    public abstract void swap(Location bot1, Location top1, Location bot2, Location top2,
        Runnable whenDone);

    public abstract void setBiome(CuboidRegion region, int extendBiome, BiomeType biome,
        String world, Runnable whenDone);
}
