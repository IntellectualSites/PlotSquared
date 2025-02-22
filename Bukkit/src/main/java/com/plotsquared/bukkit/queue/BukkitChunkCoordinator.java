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
package com.plotsquared.bukkit.queue;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.queue.ChunkCoordinator;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;
import io.papermc.lib.PaperLib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Utility that allows for the loading and coordination of chunk actions
 * <p>
 * The coordinator takes in collection of chunk coordinates, loads them
 * and allows the caller to specify a sink for the loaded chunks. The
 * coordinator will prevent the chunks from being unloaded until the sink
 * has fully consumed the chunk
 * </p>
 **/
public final class BukkitChunkCoordinator extends ChunkCoordinator {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + BukkitChunkCoordinator.class.getSimpleName());

    private final List<ProgressSubscriber> progressSubscribers = new LinkedList<>();

    private final Queue<BlockVector2> requestedChunks;
    private final Queue<Chunk> availableChunks;
    private final long maxIterationTime;
    private final Plugin plugin;
    private final Consumer<BlockVector2> chunkConsumer;
    private final org.bukkit.World bukkitWorld;
    private final Runnable whenDone;
    private final Consumer<Throwable> throwableConsumer;
    private final boolean unloadAfter;
    private final int totalSize;
    private final AtomicInteger expectedSize;
    private final AtomicInteger loadingChunks = new AtomicInteger();
    private final boolean forceSync;
    private final boolean shouldGen;

    private int batchSize;
    private PlotSquaredTask task;
    private volatile boolean shouldCancel;
    private boolean finished;

    @Inject
    private BukkitChunkCoordinator(
            @Assisted final long maxIterationTime,
            @Assisted final int initialBatchSize,
            @Assisted final @NonNull Consumer<BlockVector2> chunkConsumer,
            @Assisted final @NonNull World world,
            @Assisted final @NonNull Collection<BlockVector2> requestedChunks,
            @Assisted final @NonNull Runnable whenDone,
            @Assisted final @NonNull Consumer<Throwable> throwableConsumer,
            @Assisted("unloadAfter") final boolean unloadAfter,
            @Assisted final @NonNull Collection<ProgressSubscriber> progressSubscribers,
            @Assisted("forceSync") final boolean forceSync,
            @Assisted("shouldGen") final boolean shouldGen
    ) {
        this.requestedChunks = new LinkedBlockingQueue<>(requestedChunks);
        this.availableChunks = new LinkedBlockingQueue<>();
        this.totalSize = requestedChunks.size();
        this.expectedSize = new AtomicInteger(this.totalSize);
        this.batchSize = initialBatchSize;
        this.chunkConsumer = chunkConsumer;
        this.maxIterationTime = maxIterationTime;
        this.whenDone = whenDone;
        this.throwableConsumer = throwableConsumer;
        this.unloadAfter = unloadAfter;
        this.plugin = JavaPlugin.getPlugin(BukkitPlatform.class);
        this.bukkitWorld = Bukkit.getWorld(world.getName());
        this.progressSubscribers.addAll(progressSubscribers);
        this.forceSync = forceSync;
        this.shouldGen = shouldGen;
    }

    @Override
    public void start() {
        if (!forceSync) {
            // Request initial batch
            this.requestBatch();
            // Wait until next tick to give the chunks a chance to be loaded
            TaskManager.runTaskLater(() -> task = TaskManager.runTaskRepeat(this, TaskTime.ticks(1)), TaskTime.ticks(1));
        } else {
            try {
                while (!shouldCancel && !requestedChunks.isEmpty()) {
                    chunkConsumer.accept(requestedChunks.poll());
                }
            } catch (Throwable t) {
                throwableConsumer.accept(t);
            } finally {
                finish();
            }
        }
    }

    @Override
    public void cancel() {
        shouldCancel = true;
    }

    private void finish() {
        try {
            this.whenDone.run();
        } catch (final Throwable throwable) {
            this.throwableConsumer.accept(throwable);
        } finally {
            for (final ProgressSubscriber subscriber : this.progressSubscribers) {
                subscriber.notifyEnd();
            }
            if (task != null) {
                task.cancel();
            }
            finished = true;
        }
    }

    @Override
    public void run() {
        if (shouldCancel) {
            if (unloadAfter) {
                Chunk chunk;
                while ((chunk = availableChunks.poll()) != null) {
                    freeChunk(chunk);
                }
            }
            finish();
            return;
        }

        Chunk chunk = this.availableChunks.poll();
        if (chunk == null) {
            if (this.availableChunks.isEmpty()) {
                if (this.requestedChunks.isEmpty() && loadingChunks.get() == 0) {
                    finish();
                } else {
                    requestBatch();
                }
            }
            return;
        }
        long[] iterationTime = new long[2];
        int processedChunks = 0;
        do {
            final long start = System.currentTimeMillis();
            try {
                this.chunkConsumer.accept(BlockVector2.at(chunk.getX(), chunk.getZ()));
            } catch (final Throwable throwable) {
                this.throwableConsumer.accept(throwable);
            }
            if (unloadAfter) {
                this.freeChunk(chunk);
            }
            processedChunks++;
            final long end = System.currentTimeMillis();
            // Update iteration time
            iterationTime[0] = iterationTime[1];
            iterationTime[1] = end - start;
        } while (iterationTime[0] + iterationTime[1] < this.maxIterationTime * 2 && (chunk = availableChunks.poll()) != null);
        if (processedChunks < this.batchSize) {
            // Adjust batch size based on the amount of processed chunks per tick
            this.batchSize = processedChunks;
        }

        final int expected = this.expectedSize.addAndGet(-processedChunks);

        if (expected <= 0) {
            finish();
        } else {
            if (this.availableChunks.size() < processedChunks) {
                final double progress = ((double) totalSize - (double) expected) / (double) totalSize;
                for (final ProgressSubscriber subscriber : this.progressSubscribers) {
                    subscriber.notifyProgress(this, progress);
                }
                this.requestBatch();
            }
        }
    }

    /**
     * Requests a batch of chunks to be loaded
     */
    private void requestBatch() {
        for (int i = 0; i < this.batchSize && this.requestedChunks.peek() != null; i++) {
            // This required PaperLib to be bumped to version 1.0.4 to mark the request as urgent
            final BlockVector2 chunk = this.requestedChunks.poll();
            loadingChunks.incrementAndGet();
            PaperLib
                    .getChunkAtAsync(this.bukkitWorld, chunk.getX(), chunk.getZ(), shouldGen, true)
                    .completeOnTimeout(null, 10L, TimeUnit.SECONDS)
                    .whenComplete((chunkObject, throwable) -> {
                        loadingChunks.decrementAndGet();
                        if (throwable != null) {
                            LOGGER.error("Failed to load chunk {}", chunk, throwable);
                            // We want one less because this couldn't be processed
                            this.expectedSize.decrementAndGet();
                        } else if (chunkObject == null) {
                            LOGGER.warn("Timed out awaiting chunk load {}", chunk);
                            this.requestedChunks.offer(chunk);
                        } else if (PlotSquared.get().isMainThread(Thread.currentThread())) {
                            this.processChunk(chunkObject);
                        } else {
                            TaskManager.runTask(() -> this.processChunk(chunkObject));
                        }
                    });
        }
    }

    /**
     * Once a chunk has been loaded, process it (add a plugin ticket and add to
     * available chunks list). It is important that this gets executed on the
     * server's main thread.
     */
    private void processChunk(final @NonNull Chunk chunk) {
        /* Chunk#isLoaded does not necessarily return true shortly after PaperLib#getChunkAtAsync completes, but the chunk is
        still loaded.
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException(String.format("Chunk %d;%d is is not loaded", chunk.getX(), chunk.getZ());
        }*/
        if (finished) {
            return;
        }
        chunk.addPluginChunkTicket(this.plugin);
        this.availableChunks.add(chunk);
    }

    /**
     * Once a chunk has been used, free it up for unload by removing the plugin ticket
     */
    private void freeChunk(final @NonNull Chunk chunk) {
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException(String.format("Chunk %d;%d is is not loaded", chunk.getX(), chunk.getZ()));
        }
        chunk.removePluginChunkTicket(this.plugin);
    }

    @Override
    public int getRemainingChunks() {
        return this.expectedSize.get();
    }

    @Override
    public int getTotalChunks() {
        return this.totalSize;
    }

    /**
     * Subscribe to coordinator progress updates
     *
     * @param subscriber Subscriber
     */
    public void subscribeToProgress(final @NonNull ProgressSubscriber subscriber) {
        this.progressSubscribers.add(subscriber);
    }

}
