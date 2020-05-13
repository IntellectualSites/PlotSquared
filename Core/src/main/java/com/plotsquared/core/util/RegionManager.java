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
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class RegionManager {

    public static RegionManager manager = null;

    public static BlockVector2 getRegion(Location location) {
        int x = location.getX() >> 9;
        int z = location.getZ() >> 9;
        return BlockVector2.at(x, z);
    }

    public static void largeRegionTask(final String world, final CuboidRegion region,
        final RunnableVal<BlockVector2> task, final Runnable whenDone) {
        TaskManager.runTaskAsync(() -> {
            HashSet<BlockVector2> chunks = new HashSet<>();
            Set<BlockVector2> mcrs = manager.getChunkChunks(world);
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
            TaskManager.objectTask(chunks, new RunnableVal<BlockVector2>() {
                @Override public void run(BlockVector2 value) {
                    ChunkManager.manager.loadChunk(world, value, false)
                        .thenRun(() -> task.run(value));
                }
            }, whenDone);
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
            new File(PlotSquared.get().IMP.getWorldContainer(), world + File.separator + "region");
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
                File file = new File(PlotSquared.get().IMP.getWorldContainer(), directory);
                PlotSquared.log("&6 - Deleting file: " + file.getName() + " (max 1024 chunks)");
                if (file.exists()) {
                    file.delete();
                }
            }
            TaskManager.runTask(whenDone);
        });
    }

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
