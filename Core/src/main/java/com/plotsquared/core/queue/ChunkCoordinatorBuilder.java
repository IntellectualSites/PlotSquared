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
package com.plotsquared.core.queue;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds a {@link ChunkCoordinator} instance
 */
public class ChunkCoordinatorBuilder {

    private final List<BlockVector2> requestedChunks = new LinkedList<>();
    private final List<ProgressSubscriber> progressSubscribers = new ArrayList<>();
    private final ChunkCoordinatorFactory chunkCoordinatorFactory;
    private Consumer<Throwable> throwableConsumer = Throwable::printStackTrace;
    private World world;
    private Consumer<BlockVector2> chunkConsumer;
    private Runnable whenDone = () -> {
    };
    private long maxIterationTime = Settings.QUEUE.MAX_ITERATION_TIME; // A little over 1 tick;
    private int initialBatchSize = Settings.QUEUE.INITIAL_BATCH_SIZE;
    private boolean unloadAfter = true;

    @Inject public ChunkCoordinatorBuilder(@Nonnull ChunkCoordinatorFactory chunkCoordinatorFactory) {
        this.chunkCoordinatorFactory = chunkCoordinatorFactory;
    }

    /**
     * Set the world
     *
     * @param world world
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder inWorld(@Nonnull final World world) {
        this.world = Preconditions.checkNotNull(world, "World may not be null");
        return this;
    }

    /**
     * Add a chunk to be accessed
     *
     * @param chunkLocation BlockVector2 of chunk to add
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withChunk(@Nonnull final BlockVector2 chunkLocation) {
        this.requestedChunks.add(Preconditions.checkNotNull(chunkLocation, "Chunk location may not be null"));
        return this;
    }

    /**
     * Add a Collection of chunks to be accessed
     *
     * @param chunkLocations Collection of BlockVector2 to add
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withChunks(@Nonnull final Collection<BlockVector2> chunkLocations) {
        chunkLocations.forEach(this::withChunk);
        return this;
    }

    /**
     * Add chunks within a region to be accessed
     *
     * @param pos1 minimum region location
     * @param pos2 maximum region location
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withRegion(@Nonnull Location pos1, @Nonnull Location pos2) {
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

        chunks.forEach(this::withChunk);
        return this;
    }

    /**
     * Set the consumer to be used when a chunk is loaded
     *
     * @param chunkConsumer Consumer to be used by the ChunkCoordinator
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withConsumer(@Nonnull final Consumer<BlockVector2> chunkConsumer) {
        this.chunkConsumer = Preconditions.checkNotNull(chunkConsumer, "Chunk consumer may not be null");
        return this;
    }

    /**
     * Set the Runnable to run when all chunks have been accessed
     *
     * @param whenDone task to run when all chunks are accessed
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withFinalAction(@Nullable final Runnable whenDone) {
        if (whenDone == null) {
            return this;
        }
        this.whenDone = whenDone;
        return this;
    }

    /**
     * Set the max time taken while iterating over and accessing loaded chunks
     *
     * @param maxIterationTime max iteration time
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withMaxIterationTime(final long maxIterationTime) {
        Preconditions.checkArgument(maxIterationTime > 0, "Max iteration time must be positive");
        this.maxIterationTime = maxIterationTime;
        return this;
    }

    /**
     * Set the initial batch size to be used for loading chunks
     *
     * @param initialBatchSize initial batch size
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withInitialBatchSize(final int initialBatchSize) {
        Preconditions.checkArgument(initialBatchSize > 0, "Initial batch size must be positive");
        this.initialBatchSize = initialBatchSize;
        return this;
    }

    /**
     * Set the consumer to be used to handle {@link Throwable}s
     *
     * @param throwableConsumer consumer to hanble throwables
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder withThrowableConsumer(@Nonnull final Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer = Preconditions.checkNotNull(throwableConsumer, "Throwable consumer may not be null");
        return this;
    }

    /**
     * Set whether the chunks should be allow to unload after being accessed. This should only be used where the chunks are read from
     * and then written to from a separate queue where they're consequently unloaded.
     *
     * @param unloadAfter if to unload chuns afterwards
     * @return this ChunkCoordinatorBuilder instance
     */
    @Nonnull public ChunkCoordinatorBuilder unloadAfter(final boolean unloadAfter) {
        this.unloadAfter = unloadAfter;
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withProgressSubscriber(ProgressSubscriber progressSubscriber) {
        this.progressSubscribers.add(progressSubscriber);
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withProgressSubscribers(Collection<ProgressSubscriber> progressSubscribers) {
        this.progressSubscribers.addAll(progressSubscribers);
        return this;
    }

    /**
     * Create a new {@link ChunkCoordinator} instance based on the values in the Builder instance.
     *
     * @return a new ChunkCoordinator
     */
    @Nonnull public ChunkCoordinator build() {
        Preconditions.checkNotNull(this.world, "No world was supplied");
        Preconditions.checkNotNull(this.chunkConsumer, "No chunk consumer was supplied");
        Preconditions.checkNotNull(this.whenDone, "No final action was supplied");
        Preconditions.checkNotNull(this.throwableConsumer, "No throwable consumer was supplied");
        return chunkCoordinatorFactory
            .create(this.maxIterationTime, this.initialBatchSize, this.chunkConsumer, this.world, this.requestedChunks, this.whenDone,
                this.throwableConsumer, this.unloadAfter, this.progressSubscribers);
    }

}
