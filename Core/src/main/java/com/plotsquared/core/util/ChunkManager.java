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
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ChunkManager {

    private static final Map<BlockVector2, RunnableVal<ScopedLocalBlockQueue>> forceChunks =
        new ConcurrentHashMap<>();
    private static final Map<BlockVector2, RunnableVal<ScopedLocalBlockQueue>> addChunks =
        new ConcurrentHashMap<>();
    public static ChunkManager manager = null;

    public static void setChunkInPlotArea(RunnableVal<ScopedLocalBlockQueue> force,
        RunnableVal<ScopedLocalBlockQueue> add, String world, BlockVector2 loc) {
        LocalBlockQueue queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
        if (PlotSquared.get().getPlotAreaManager().isAugmented(world) && PlotSquared.get().isNonStandardGeneration(world, loc)) {
            int blockX = loc.getX() << 4;
            int blockZ = loc.getZ() << 4;
            ScopedLocalBlockQueue scoped =
                new ScopedLocalBlockQueue(queue, new Location(world, blockX, 0, blockZ),
                    new Location(world, blockX + 15, 255, blockZ + 15));
            if (force != null) {
                force.run(scoped);
            } else {
                scoped.regenChunk(loc.getX(), loc.getZ());
                if (add != null) {
                    add.run(scoped);
                }
            }
            queue.flush();
        } else {
            if (force != null) {
                forceChunks.put(loc, force);
            }
            addChunks.put(loc, add);
            queue.regenChunk(loc.getX(), loc.getZ());
            forceChunks.remove(loc);
            addChunks.remove(loc);
        }
    }

    public static boolean preProcessChunk(BlockVector2 loc, ScopedLocalBlockQueue queue) {
        final RunnableVal<ScopedLocalBlockQueue> forceChunk = forceChunks.get(loc);
        if (forceChunk != null) {
            forceChunk.run(queue);
            forceChunks.remove(loc);
            return true;
        }
        return false;
    }

    public static boolean postProcessChunk(BlockVector2 loc, ScopedLocalBlockQueue queue) {
        final RunnableVal<ScopedLocalBlockQueue> addChunk = forceChunks.get(loc);
        if (addChunk != null) {
            addChunk.run(queue);
            addChunks.remove(loc);
            return true;
        }
        return false;
    }

    public static void chunkTask(final Plot plot, final RunnableVal<int[]> task,
        final Runnable whenDone, final int allocate) {
        final ArrayList<CuboidRegion> regions = new ArrayList<>(plot.getRegions());
        Runnable smallTask = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    TaskManager.runTask(whenDone);
                    return;
                }
                CuboidRegion value = regions.remove(0);
                Location pos1 = new Location(plot.getWorldName(), value.getMinimumPoint().getX(), 0,
                    value.getMinimumPoint().getZ());
                Location pos2 = new Location(plot.getWorldName(), value.getMaximumPoint().getX(), 0,
                    value.getMaximumPoint().getZ());
                chunkTask(pos1, pos2, task, this, allocate);
            }
        };
        smallTask.run();
    }

    /**
     * The int[] will be in the form: [chunkX, chunkZ, pos1x, pos1z, pos2x, pos2z, isEdge] and will represent the bottom and top parts of the chunk
     *
     * @param pos1
     * @param pos2
     * @param task
     * @param whenDone
     */
    public static void chunkTask(Location pos1, Location pos2, final RunnableVal<int[]> task,
        final Runnable whenDone, final int allocate) {
        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;
        final ArrayList<BlockVector2> chunks = new ArrayList<>();

        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(BlockVector2.at(x, z));
            }
        }
        TaskManager.runTask(new Runnable() {
            @Override public void run() {
                long start = System.currentTimeMillis();
                while (!chunks.isEmpty() && ((System.currentTimeMillis() - start) < allocate)) {
                    BlockVector2 chunk = chunks.remove(0);
                    task.value = new int[7];
                    task.value[0] = chunk.getX();
                    task.value[1] = chunk.getZ();
                    task.value[2] = task.value[0] << 4;
                    task.value[3] = task.value[1] << 4;
                    task.value[4] = task.value[2] + 15;
                    task.value[5] = task.value[3] + 15;
                    if (task.value[0] == bcx) {
                        task.value[2] = p1x;
                        task.value[6] = 1;
                    }
                    if (task.value[0] == tcx) {
                        task.value[4] = p2x;
                        task.value[6] = 1;
                    }
                    if (task.value[1] == bcz) {
                        task.value[3] = p1z;
                        task.value[6] = 1;
                    }
                    if (task.value[1] == tcz) {
                        task.value[5] = p2z;
                        task.value[6] = 1;
                    }
                    task.run();
                }
                if (!chunks.isEmpty()) {
                    TaskManager.runTaskLater(this, 1);
                } else {
                    TaskManager.runTask(whenDone);
                }
            }
        });
    }

    public abstract CompletableFuture loadChunk(String world, BlockVector2 loc, boolean force);

    public abstract void unloadChunk(String world, BlockVector2 loc, boolean save);

    public Plot hasPlot(String world, BlockVector2 chunk) {
        int x1 = chunk.getX() << 4;
        int z1 = chunk.getZ() << 4;
        int x2 = x1 + 15;
        int z2 = z1 + 15;
        Location bot = new Location(world, x1, 0, z1);
        Plot plot = bot.getOwnedPlotAbs();
        if (plot != null) {
            return plot;
        }
        Location top = new Location(world, x2, 0, z2);
        plot = top.getOwnedPlotAbs();
        return plot;
    }

}
