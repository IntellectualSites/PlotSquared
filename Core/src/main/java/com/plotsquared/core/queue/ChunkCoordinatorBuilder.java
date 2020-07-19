package com.plotsquared.core.queue;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.location.Location;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ChunkCoordinatorBuilder {

    private final List<BlockVector2> requestedChunks = new LinkedList<>();
    private final ChunkCoordinatorFactory chunkCoordinatorFactory;
    private Consumer<Throwable> throwableConsumer = Throwable::printStackTrace;
    private World world;
    private Consumer<BlockVector2> chunkConsumer;
    private Runnable whenDone = () -> {
    };
    private long maxIterationTime = 60; // A little over 1 tick;
    private int initialBatchSize = 4;

    @Inject
    public ChunkCoordinatorBuilder(@Nonnull ChunkCoordinatorFactory chunkCoordinatorFactory) {
        this.chunkCoordinatorFactory = chunkCoordinatorFactory;
    }

    @NotNull public ChunkCoordinatorBuilder inWorld(@NotNull final World world) {
        this.world = Preconditions.checkNotNull(world, "World may not be null");
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withChunk(@NotNull final BlockVector2 chunkLocation) {
        this.requestedChunks
            .add(Preconditions.checkNotNull(chunkLocation, "Chunk location may not be null"));
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withChunks(
        @NotNull final Collection<BlockVector2> chunkLocations) {
        chunkLocations.forEach(this::withChunk);
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withRegion(Location pos1, Location pos2) {
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

    @NotNull public ChunkCoordinatorBuilder withConsumer(
        @NotNull final Consumer<BlockVector2> chunkConsumer) {
        this.chunkConsumer =
            Preconditions.checkNotNull(chunkConsumer, "Chunk consumer may not be null");
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withFinalAction(@NotNull final Runnable whenDone) {
        this.whenDone = Preconditions.checkNotNull(whenDone, "Final action may not be null");
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withMaxIterationTime(final long maxIterationTime) {
        Preconditions.checkArgument(maxIterationTime > 0, "Max iteration time must be positive");
        this.maxIterationTime = maxIterationTime;
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withInitialBatchSize(final int initialBatchSize) {
        Preconditions.checkArgument(initialBatchSize > 0, "Initial batch size must be positive");
        this.initialBatchSize = initialBatchSize;
        return this;
    }

    @NotNull public ChunkCoordinatorBuilder withThrowableConsumer(
        @NotNull final Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer =
            Preconditions.checkNotNull(throwableConsumer, "Throwable consumer may not be null");
        return this;
    }

    @NotNull public ChunkCoordinator build() {
        Preconditions.checkNotNull(this.world, "No world was supplied");
        Preconditions.checkNotNull(this.chunkConsumer, "No chunk consumer was supplied");
        Preconditions.checkNotNull(this.whenDone, "No final action was supplied");
        Preconditions.checkNotNull(this.throwableConsumer, "No throwable consumer was supplied");
        return chunkCoordinatorFactory
            .create(this.maxIterationTime, this.initialBatchSize, this.chunkConsumer, this.world,
                this.requestedChunks, this.whenDone, this.throwableConsumer);
    }

}
