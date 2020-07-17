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
package com.plotsquared.bukkit.queue;

import com.google.common.base.Preconditions;
import com.plotsquared.bukkit.BukkitPlatform;
import com.sk89q.worldedit.math.BlockVector2;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Utility that allows for the loading and coordination of chunk actions
 * <p>
 * The coordinator takes in collection of chunk coordinates, loads them
 * and allows the caller to specify a sink for the loaded chunks. The
 * coordinator will prevent the chunks from being unloaded until the sink
 * has fully consumed the chunk
 * <p>
 * Usage:
 * <pre>{@code
 * final ChunkCoordinator chunkCoordinator = ChunkCoordinator.builder()
 *     .inWorld(Objects.requireNonNull(Bukkit.getWorld("world"))).withChunk(BlockVector2.at(0, 0))
 *     .withConsumer(chunk -> System.out.printf("Got chunk %d;%d", chunk.getX(), chunk.getZ()))
 *     .withFinalAction(() -> System.out.println("All chunks have been loaded"))
 *     .withThrowableConsumer(throwable -> System.err.println("Something went wrong... =("))
 *     .withMaxIterationTime(25L)
 *     .build();
 * chunkCoordinator.subscribeToProgress((coordinator, progress) ->
 *     System.out.printf("Progress: %.1f", progress * 100.0f));
 * chunkCoordinator.start();
 * }</pre>
 *
 * @author Alexander Söderberg
 * @see #builder() To create a new coordinator instance
 */
public final class BukkitChunkCoordinator extends BukkitRunnable {

    private final List<ProgressSubscriber> progressSubscribers = new LinkedList<>();

    private final Queue<BlockVector2> requestedChunks;
    private final Queue<Chunk> availableChunks;
    private final long maxIterationTime;
    private final Plugin plugin;
    private final Consumer<Chunk> chunkConsumer;
    private final World world;
    private final Runnable whenDone;
    private final Consumer<Throwable> throwableConsumer;
    private final int totalSize;

    private AtomicInteger expectedSize;
    private int batchSize;

    private BukkitChunkCoordinator(final long maxIterationTime, final int initialBatchSize,
        @NotNull final Consumer<Chunk> chunkConsumer, @NotNull final World world,
        @NotNull final Collection<BlockVector2> requestedChunks, @NotNull final Runnable whenDone,
        @NotNull final Consumer<Throwable> throwableConsumer) {
        this.requestedChunks = new LinkedBlockingQueue<>(requestedChunks);
        this.availableChunks = new LinkedBlockingQueue<>();
        this.totalSize = requestedChunks.size();
        this.expectedSize = new AtomicInteger(this.totalSize);
        this.world = world;
        this.batchSize = initialBatchSize;
        this.chunkConsumer = chunkConsumer;
        this.maxIterationTime = maxIterationTime;
        this.whenDone = whenDone;
        this.throwableConsumer = throwableConsumer;
        this.plugin = JavaPlugin.getPlugin(BukkitPlatform.class);
    }

    /**
     * Create a new {@link BukkitChunkCoordinator} instance
     *
     * @return Coordinator builder instance
     */
    @NotNull public static ChunkCoordinatorBuilder builder() {
        return new ChunkCoordinatorBuilder();
    }

    /**
     * Start the coordinator instance
     */
    public void start() {
        // Request initial batch
        this.requestBatch();
        // Wait until next tick to give the chunks a chance to be loaded
        this.runTaskTimer(this.plugin, 1L, 1L);
    }

    @Override public void run() {
        Chunk chunk = this.availableChunks.poll();
        if (chunk == null) {
            return;
        }
        long iterationTime;
        int processedChunks = 0;
        do {
            final long start = System.currentTimeMillis();
            try {
                this.chunkConsumer.accept(chunk);
            } catch (final Throwable throwable) {
                this.throwableConsumer.accept(throwable);
            }
            this.freeChunk(chunk);
            processedChunks++;
            final long end = System.currentTimeMillis();
            // Update iteration time
            iterationTime = end - start;
        } while (2 * iterationTime /* last chunk + next chunk */ < this.maxIterationTime
            && (chunk = availableChunks.poll()) != null);
        if (processedChunks < this.batchSize) {
            // Adjust batch size based on the amount of processed chunks per tick
            this.batchSize = processedChunks;
        }

        final int expected = this.expectedSize.addAndGet(-processedChunks);

        final float progress = ((float) totalSize - (float) expected) / (float) totalSize;
        for (final ProgressSubscriber subscriber : this.progressSubscribers) {
            subscriber.notifyProgress(this, progress);
        }

        if (expected <= 0) {
            try {
                this.whenDone.run();
            } catch (final Throwable throwable) {
                this.throwableConsumer.accept(throwable);
            }
            this.cancel();
        } else {
            if (this.availableChunks.size() < processedChunks) {
                this.requestBatch();
            }
        }
    }

    private void requestBatch() {
        BlockVector2 chunk;
        for (int i = 0; i < this.batchSize && (chunk = this.requestedChunks.poll()) != null; i++) {
            // This required PaperLib to be bumped to version 1.0.4 to mark the request as urgent
            PaperLib.getChunkAtAsync(this.world, chunk.getX(), chunk.getZ(), true, true)
                .whenComplete((chunkObject, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        // We want one less because this couldn't be processed
                        this.expectedSize.decrementAndGet();
                    } else {
                        this.processChunk(chunkObject);
                    }
                });
        }
    }

    private void processChunk(@NotNull final Chunk chunk) {
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException(
                String.format("Chunk %d;%d is is not loaded", chunk.getX(), chunk.getZ()));
        }
        chunk.addPluginChunkTicket(this.plugin);
        this.availableChunks.add(chunk);
    }

    private void freeChunk(@NotNull final Chunk chunk) {
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException(
                String.format("Chunk %d;%d is is not loaded", chunk.getX(), chunk.getZ()));
        }
        chunk.removePluginChunkTicket(this.plugin);
    }

    /**
     * Get the amount of remaining chunks (at the time of the method call)
     *
     * @return Snapshot view of remaining chunk count
     */
    public int getRemainingChunks() {
        return this.expectedSize.get();
    }

    /**
     * Get the amount of requested chunks
     *
     * @return Requested chunk count
     */
    public int getTotalChunks() {
        return this.totalSize;
    }

    /**
     * Subscribe to coordinator progress updates
     *
     * @param subscriber Subscriber
     */
    public void subscribeToProgress(
        @NotNull final BukkitChunkCoordinator.ProgressSubscriber subscriber) {
        this.progressSubscribers.add(subscriber);
    }


    @FunctionalInterface
    public interface ProgressSubscriber {

        /**
         * Notify about a progress update in the coordinator
         *
         * @param coordinator Coordinator instance that triggered the notification
         * @param progress    Progress in the range [0, 1]
         */
        void notifyProgress(@NotNull final BukkitChunkCoordinator coordinator,
            final float progress);

    }


    public static final class ChunkCoordinatorBuilder {

        private final List<BlockVector2> requestedChunks = new LinkedList<>();
        private Consumer<Throwable> throwableConsumer = Throwable::printStackTrace;
        private World world;
        private Consumer<Chunk> chunkConsumer;
        private Runnable whenDone = () -> {
        };
        private long maxIterationTime = 60; // A little over 1 tick;
        private int initialBatchSize = 4;

        private ChunkCoordinatorBuilder() {
        }

        @NotNull public ChunkCoordinatorBuilder inWorld(@NotNull final World world) {
            this.world = Preconditions.checkNotNull(world, "World may not be null");
            return this;
        }

        @NotNull
        public ChunkCoordinatorBuilder withChunk(@NotNull final BlockVector2 chunkLocation) {
            this.requestedChunks
                .add(Preconditions.checkNotNull(chunkLocation, "Chunk location may not be null"));
            return this;
        }

        @NotNull public ChunkCoordinatorBuilder withChunks(
            @NotNull final Collection<BlockVector2> chunkLocations) {
            chunkLocations.forEach(this::withChunk);
            return this;
        }

        @NotNull
        public ChunkCoordinatorBuilder withConsumer(@NotNull final Consumer<Chunk> chunkConsumer) {
            this.chunkConsumer =
                Preconditions.checkNotNull(chunkConsumer, "Chunk consumer may not be null");
            return this;
        }

        @NotNull public ChunkCoordinatorBuilder withFinalAction(@NotNull final Runnable whenDone) {
            this.whenDone = Preconditions.checkNotNull(whenDone, "Final action may not be null");
            return this;
        }

        @NotNull public ChunkCoordinatorBuilder withMaxIterationTime(final long maxIterationTime) {
            Preconditions
                .checkArgument(maxIterationTime > 0, "Max iteration time must be positive");
            this.maxIterationTime = maxIterationTime;
            return this;
        }

        @NotNull public ChunkCoordinatorBuilder withInitialBatchSize(final int initialBatchSize) {
            Preconditions
                .checkArgument(initialBatchSize > 0, "Initial batch size must be positive");
            this.initialBatchSize = initialBatchSize;
            return this;
        }

        @NotNull public ChunkCoordinatorBuilder withThrowableConsumer(
            @NotNull final Consumer<Throwable> throwableConsumer) {
            this.throwableConsumer =
                Preconditions.checkNotNull(throwableConsumer, "Throwable consumer may not be null");
            return this;
        }

        @NotNull public BukkitChunkCoordinator build() {
            Preconditions.checkNotNull(this.world, "No world was supplied");
            Preconditions.checkNotNull(this.chunkConsumer, "No chunk consumer was supplied");
            Preconditions.checkNotNull(this.whenDone, "No final action was supplied");
            Preconditions
                .checkNotNull(this.throwableConsumer, "No throwable consumer was supplied");
            return new BukkitChunkCoordinator(this.maxIterationTime, this.initialBatchSize,
                this.chunkConsumer, this.world, this.requestedChunks, this.whenDone,
                this.throwableConsumer);
        }

    }

}
