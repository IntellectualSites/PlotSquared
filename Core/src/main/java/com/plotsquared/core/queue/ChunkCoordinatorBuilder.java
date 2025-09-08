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
package com.plotsquared.core.queue;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    private boolean forceSync = false;
    private boolean shouldGen = true;

    @Inject
    public ChunkCoordinatorBuilder(@NonNull ChunkCoordinatorFactory chunkCoordinatorFactory) {
        this.chunkCoordinatorFactory = chunkCoordinatorFactory;
    }

    /**
     * Set the world
     *
     * @param world world
     * @return this ChunkCoordinatorBuilder instance
     */
    public @NonNull ChunkCoordinatorBuilder inWorld(final @NonNull World world) {
        this.world = Preconditions.checkNotNull(world, "World may not be null");
        return this;
    }

    /**
     * Add a chunk to be accessed
     *
     * @param chunkLocation BlockVector2 of chunk to add
     * @return this ChunkCoordinatorBuilder instance
     */
    public @NonNull ChunkCoordinatorBuilder withChunk(final @NonNull BlockVector2 chunkLocation) {
        this.requestedChunks.add(Preconditions.checkNotNull(chunkLocation, "Chunk location may not be null"));
        return this;
    }

    /**
     * Add a Collection of chunks to be accessed
     *
     * @param chunkLocations Collection of BlockVector2 to add
     * @return this ChunkCoordinatorBuilder instance
     */
    public @NonNull ChunkCoordinatorBuilder withChunks(final @NonNull Collection<BlockVector2> chunkLocations) {
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
    public @NonNull ChunkCoordinatorBuilder withRegion(@NonNull Location pos1, @NonNull Location pos2) {
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
    public @NonNull ChunkCoordinatorBuilder withConsumer(final @NonNull Consumer<BlockVector2> chunkConsumer) {
        this.chunkConsumer = Preconditions.checkNotNull(chunkConsumer, "Chunk consumer may not be null");
        return this;
    }

    /**
     * Set the Runnable to run when all chunks have been accessed
     *
     * @param whenDone task to run when all chunks are accessed
     * @return this ChunkCoordinatorBuilder instance
     */
    public @NonNull ChunkCoordinatorBuilder withFinalAction(final @Nullable Runnable whenDone) {
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
    public @NonNull ChunkCoordinatorBuilder withMaxIterationTime(final long maxIterationTime) {
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
    public @NonNull ChunkCoordinatorBuilder withInitialBatchSize(final int initialBatchSize) {
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
    public @NonNull ChunkCoordinatorBuilder withThrowableConsumer(final @NonNull Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer = Preconditions.checkNotNull(throwableConsumer, "Throwable consumer may not be null");
        return this;
    }

    /**
     * Set whether the chunks should be allow to unload after being accessed. This should only be used where the chunks are read from
     * and then written to from a separate queue where they're consequently unloaded.
     *
     * @param unloadAfter if to unload chunks afterwards
     * @return this ChunkCoordinatorBuilder instance
     */
    public @NonNull ChunkCoordinatorBuilder unloadAfter(final boolean unloadAfter) {
        this.unloadAfter = unloadAfter;
        return this;
    }

    /**
     * Set whether the chunks coordinator should be forced to be synchronous. This is not necessarily synchronous to the server,
     * and simply effectively makes {@link ChunkCoordinator#start()} ()} a blocking operation.
     *
     * @param forceSync force sync or not
     * @since 6.9.0
     */
    public @NonNull ChunkCoordinatorBuilder forceSync(final boolean forceSync) {
        this.forceSync = forceSync;
        return this;
    }

    /**
     * Set whether chunks should be generated as part of this operation. Default is true. Disabling this may not be supported
     * depending on server implementation. (i.e. setting to false may not actually disable generation as part of this operation
     * - this is just a catch-all in case of future differing server implementations; the option will work on Spigot/Paper).
     *
     * @param shouldGen should generate new chunks or not
     * @since 7.5.0
     */
    public @NonNull ChunkCoordinatorBuilder shouldGen(final boolean shouldGen) {
        this.shouldGen = shouldGen;
        return this;
    }

    public @NonNull ChunkCoordinatorBuilder withProgressSubscriber(ProgressSubscriber progressSubscriber) {
        this.progressSubscribers.add(progressSubscriber);
        return this;
    }

    public @NonNull ChunkCoordinatorBuilder withProgressSubscribers(Collection<ProgressSubscriber> progressSubscribers) {
        this.progressSubscribers.addAll(progressSubscribers);
        return this;
    }

    /**
     * Create a new {@link ChunkCoordinator} instance based on the values in the Builder instance.
     *
     * @return a new ChunkCoordinator
     */
    public @NonNull ChunkCoordinator build() {
        Preconditions.checkNotNull(this.world, "No world was supplied");
        Preconditions.checkNotNull(this.chunkConsumer, "No chunk consumer was supplied");
        Preconditions.checkNotNull(this.whenDone, "No final action was supplied");
        Preconditions.checkNotNull(this.throwableConsumer, "No throwable consumer was supplied");
        return chunkCoordinatorFactory
                .create(
                        this.maxIterationTime,
                        this.initialBatchSize,
                        this.chunkConsumer,
                        this.world,
                        this.requestedChunks,
                        this.whenDone,
                        this.throwableConsumer,
                        this.unloadAfter,
                        this.progressSubscribers,
                        this.forceSync,
                        this.shouldGen
                );
    }

}
