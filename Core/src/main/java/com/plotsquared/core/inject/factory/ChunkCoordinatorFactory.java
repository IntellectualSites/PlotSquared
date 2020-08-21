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
package com.plotsquared.core.inject.factory;

import com.plotsquared.core.queue.ChunkCoordinator;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Consumer;

public interface ChunkCoordinatorFactory {

    @Nonnull ChunkCoordinator create(final long maxIterationTime,
                                     final int initialBatchSize,
                                     @Nonnull final Consumer<BlockVector2> chunkConsumer,
                                     @Nonnull final World world,
                                     @Nonnull final Collection<BlockVector2> requestedChunks,
                                     @Nonnull final Runnable whenDone,
                                     @Nonnull final Consumer<Throwable> throwableConsumer,
                                     final boolean unloadAfter);

}
