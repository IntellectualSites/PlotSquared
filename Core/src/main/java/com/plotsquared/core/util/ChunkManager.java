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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.worldedit.math.BlockVector2;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ChunkManager {

    private static final Map<BlockVector2, RunnableVal<ScopedQueueCoordinator>> forceChunks = new ConcurrentHashMap<>();
    private static final Map<BlockVector2, RunnableVal<ScopedQueueCoordinator>> addChunks = new ConcurrentHashMap<>();

    public static void setChunkInPlotArea(RunnableVal<ScopedQueueCoordinator> force,
                                          RunnableVal<ScopedQueueCoordinator> add,
                                          String world,
                                          BlockVector2 loc) {
        QueueCoordinator queue = PlotSquared.platform().getGlobalBlockQueue().getNewQueue(PlotSquared.platform().getWorldUtil().getWeWorld(world));
        if (PlotSquared.get().getPlotAreaManager().isAugmented(world) && PlotSquared.get().isNonStandardGeneration(world, loc)) {
            int blockX = loc.getX() << 4;
            int blockZ = loc.getZ() << 4;
            ScopedQueueCoordinator scoped =
                new ScopedQueueCoordinator(queue, Location.at(world, blockX, 0, blockZ), Location.at(world, blockX + 15, 255, blockZ + 15));
            if (force != null) {
                force.run(scoped);
            } else {
                scoped.regenChunk(loc.getX(), loc.getZ());
                if (add != null) {
                    add.run(scoped);
                }
            }
            queue.enqueue();
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

    public static boolean preProcessChunk(BlockVector2 loc, ScopedQueueCoordinator queue) {
        final RunnableVal<ScopedQueueCoordinator> forceChunk = forceChunks.get(loc);
        if (forceChunk != null) {
            forceChunk.run(queue);
            forceChunks.remove(loc);
            return true;
        }
        return false;
    }

    public static boolean postProcessChunk(BlockVector2 loc, ScopedQueueCoordinator queue) {
        final RunnableVal<ScopedQueueCoordinator> addChunk = forceChunks.get(loc);
        if (addChunk != null) {
            addChunk.run(queue);
            addChunks.remove(loc);
            return true;
        }
        return false;
    }

    @Deprecated public abstract CompletableFuture loadChunk(String world, BlockVector2 loc, boolean force);

}
