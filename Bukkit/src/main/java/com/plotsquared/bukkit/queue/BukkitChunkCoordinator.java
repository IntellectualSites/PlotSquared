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
package com.plotsquared.bukkit.queue;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.queue.ChunkCoordinator;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.World;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
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
 * </p>
 **/
public final class BukkitChunkCoordinator extends ChunkCoordinator {

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
    private int batchSize;

    @Inject private BukkitChunkCoordinator(@Assisted final long maxIterationTime,
                                           @Assisted final int initialBatchSize,
                                           @Assisted @Nonnull final Consumer<BlockVector2> chunkConsumer,
                                           @Assisted @Nonnull final World world,
                                           @Assisted @Nonnull final Collection<BlockVector2> requestedChunks,
                                           @Assisted @Nonnull final Runnable whenDone,
                                           @Assisted @Nonnull final Consumer<Throwable> throwableConsumer,
                                           @Assisted final boolean unloadAfter,
                                           @Assisted @Nonnull final Collection<ProgressSubscriber> progressSubscribers) {
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
    }

    @Override public void start() {
        // Request initial batch
        this.requestBatch();
        // Wait until next tick to give the chunks a chance to be loaded
        TaskManager.runTaskLater(() -> TaskManager.runTaskRepeat(this, TaskTime.ticks(1)), TaskTime.ticks(1));
    }

    @Override public void runTask() {
        Chunk chunk = this.availableChunks.poll();
        if (chunk == null) {
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
            try {
                this.whenDone.run();
            } catch (final Throwable throwable) {
                this.throwableConsumer.accept(throwable);
            } finally {
                for (final ProgressSubscriber subscriber : this.progressSubscribers) {
                    subscriber.notifyEnd();
                }
                this.cancel();
            }
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
        BlockVector2 chunk;
        for (int i = 0; i < this.batchSize && (chunk = this.requestedChunks.poll()) != null; i++) {
            // This required PaperLib to be bumped to version 1.0.4 to mark the request as urgent
            PaperLib.getChunkAtAsync(this.bukkitWorld, chunk.getX(), chunk.getZ(), true, true).whenComplete((chunkObject, throwable) -> {
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

    /**
     * Once a chunk has been loaded, process it (add a plugin ticket and add to available chunks list)
     */
    private void processChunk(@Nonnull final Chunk chunk) {
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException(String.format("Chunk %d;%d is is not loaded", chunk.getX(), chunk.getZ()));
        }
        chunk.addPluginChunkTicket(this.plugin);
        this.availableChunks.add(chunk);
    }

    /**
     * Once a chunk has been used, free it up for unload by removing the plugin ticket
     */
    private void freeChunk(@Nonnull final Chunk chunk) {
        if (!chunk.isLoaded()) {
            throw new IllegalArgumentException(String.format("Chunk %d;%d is is not loaded", chunk.getX(), chunk.getZ()));
        }
        chunk.removePluginChunkTicket(this.plugin);
    }

    @Override public int getRemainingChunks() {
        return this.expectedSize.get();
    }

    @Override public int getTotalChunks() {
        return this.totalSize;
    }

    /**
     * Subscribe to coordinator progress updates
     *
     * @param subscriber Subscriber
     */
    public void subscribeToProgress(@Nonnull final ProgressSubscriber subscriber) {
        this.progressSubscribers.add(subscriber);
    }

}
