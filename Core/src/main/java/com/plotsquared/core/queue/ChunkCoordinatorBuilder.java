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
package com.plotsquared.core.queue;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.location.Location;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private boolean unloadAfter = true;

    @Inject
    public ChunkCoordinatorBuilder(@Nonnull ChunkCoordinatorFactory chunkCoordinatorFactory) {
        this.chunkCoordinatorFactory = chunkCoordinatorFactory;
    }

    @Nonnull public ChunkCoordinatorBuilder inWorld(@Nonnull final World world) {
        this.world = Preconditions.checkNotNull(world, "World may not be null");
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withChunk(@Nonnull final BlockVector2 chunkLocation) {
        this.requestedChunks
            .add(Preconditions.checkNotNull(chunkLocation, "Chunk location may not be null"));
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withChunks(
        @Nonnull final Collection<BlockVector2> chunkLocations) {
        chunkLocations.forEach(this::withChunk);
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withRegion(Location pos1, Location pos2) {
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

    @Nonnull public ChunkCoordinatorBuilder withConsumer(
        @Nonnull final Consumer<BlockVector2> chunkConsumer) {
        this.chunkConsumer =
            Preconditions.checkNotNull(chunkConsumer, "Chunk consumer may not be null");
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withFinalAction(@Nullable final Runnable whenDone) {
        if (whenDone == null) {
            return this;
        }
        this.whenDone = whenDone;
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withMaxIterationTime(final long maxIterationTime) {
        Preconditions.checkArgument(maxIterationTime > 0, "Max iteration time must be positive");
        this.maxIterationTime = maxIterationTime;
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withInitialBatchSize(final int initialBatchSize) {
        Preconditions.checkArgument(initialBatchSize > 0, "Initial batch size must be positive");
        this.initialBatchSize = initialBatchSize;
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder withThrowableConsumer(
        @Nonnull final Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer =
            Preconditions.checkNotNull(throwableConsumer, "Throwable consumer may not be null");
        return this;
    }

    @Nonnull public ChunkCoordinatorBuilder unloadAfter(final boolean unloadAfter) {
        this.unloadAfter = unloadAfter;
        return this;
    }

    @Nonnull public ChunkCoordinator build() {
        Preconditions.checkNotNull(this.world, "No world was supplied");
        Preconditions.checkNotNull(this.chunkConsumer, "No chunk consumer was supplied");
        Preconditions.checkNotNull(this.whenDone, "No final action was supplied");
        Preconditions.checkNotNull(this.throwableConsumer, "No throwable consumer was supplied");
        return chunkCoordinatorFactory
            .create(this.maxIterationTime, this.initialBatchSize, this.chunkConsumer, this.world,
                this.requestedChunks, this.whenDone, this.throwableConsumer, this.unloadAfter);
    }

}
