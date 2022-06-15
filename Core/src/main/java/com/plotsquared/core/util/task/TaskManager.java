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
package com.plotsquared.core.util.task;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task manager that handles scheduling of tasks.
 * Synchronous methods make no guarantee of being scheduled on the
 * server thread, instead they guarantee that no two synchronous
 * operations happen at the same time. Implementations of
 * the task manager might make other guarantees. All asynchronous
 * operations will happen on another thread, no matter where
 * they're scheduled from.
 */
public abstract class TaskManager {

    private static final Set<String> teleportQueue = new HashSet<>();
    private static final Map<Integer, PlotSquaredTask> tasks = new HashMap<>();
    public static AtomicInteger index = new AtomicInteger(0);

    private static TaskManager platformImplementation;

    /**
     * Add a string to the teleport queue
     *
     * @param string String to add
     */
    public static void addToTeleportQueue(final @NonNull String string) {
        teleportQueue.add(string);
    }

    /**
     * Remove a string from the teleport queue
     *
     * @param string String to remove
     *               return {@code true} if the value was stored in the map, or {@code false}
     * @return if string was actually removed
     */
    public static boolean removeFromTeleportQueue(final @NonNull String string) {
        return teleportQueue.remove(string);
    }

    /**
     * Add a task to the task map
     *
     * @param task Task
     * @param id   Task ID
     */
    public static void addTask(final @NonNull PlotSquaredTask task, final int id) {
        tasks.put(id, task);
    }

    /**
     * Remove a task from the task map and return the stored value
     *
     * @param id Task ID
     * @return Task if stored, or {@code null}
     */
    public static @Nullable PlotSquaredTask removeTask(final int id) {
        return tasks.remove(id);
    }

    /**
     * Run a repeating synchronous task. If using a platform scheduler,
     * this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    public static @NonNull PlotSquaredTask runTaskRepeat(
            final @Nullable Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        if (runnable != null) {
            if (getPlatformImplementation() == null) {
                throw new IllegalArgumentException("disabled");
            }
            return getPlatformImplementation().taskRepeat(runnable, taskTime);
        }
        return PlotSquaredTask.nullTask();
    }

    /**
     * Run an asynchronous task. This will never run on the server thread
     *
     * @param runnable Task to run
     */
    public static void runTaskAsync(final @Nullable Runnable runnable) {
        if (runnable != null) {
            if (getPlatformImplementation() == null) {
                runnable.run();
                return;
            }
            getPlatformImplementation().taskAsync(runnable);
        }
    }

    /**
     * Run a synchronous task. If using a platform scheduler, this is guaranteed
     * to run on the server thread
     *
     * @param runnable Task to run
     */
    public static void runTask(final @Nullable Runnable runnable) {
        if (runnable != null) {
            if (getPlatformImplementation() == null) {
                runnable.run();
                return;
            }
            getPlatformImplementation().task(runnable);
        }
    }

    /**
     * Run a synchronous task after a given delay.
     * If using a platform scheduler, this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public static void runTaskLater(
            final @Nullable Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        if (runnable != null) {
            if (getPlatformImplementation() == null) {
                runnable.run();
                return;
            }
            getPlatformImplementation().taskLater(runnable, taskTime);
        }
    }

    /**
     * Run an asynchronous task after a given delay. This will never
     * run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public static void runTaskLaterAsync(
            final @Nullable Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        if (runnable != null) {
            if (getPlatformImplementation() == null) {
                runnable.run();
                return;
            }
            getPlatformImplementation().taskLaterAsync(runnable, taskTime);
        }
    }

    public static @Nullable TaskManager getPlatformImplementation() {
        return platformImplementation;
    }

    public static void setPlatformImplementation(final @NonNull TaskManager implementation) {
        platformImplementation = implementation;
    }

    /**
     * Break up a series of tasks so that they can run without lagging the server
     *
     * @param objects Objects to perform the task on
     * @param task    Task to perform
     * @param <T>     Object type
     * @return Future that completes when the tasks are done
     */
    public <T> CompletableFuture<Void> objectTask(
            final @NonNull Collection<T> objects,
            final @NonNull RunnableVal<T> task
    ) {
        final Iterator<T> iterator = objects.iterator();
        final ObjectTaskRunnable<T> taskRunnable = new ObjectTaskRunnable<>(iterator, task);
        TaskManager.runTask(taskRunnable);
        return taskRunnable.getCompletionFuture();
    }

    /**
     * Make a synchronous method call and return the result
     *
     * @param function Method to call
     * @param <T>      Return type
     * @return Method result
     * @throws Exception If the call fails
     */
    public <T> T sync(final @NonNull Callable<T> function) throws Exception {
        return sync(function, Integer.MAX_VALUE);
    }

    /**
     * Make a synchronous method call and return the result
     *
     * @param function Method to call
     * @param timeout  Timeout (ms)
     * @param <T>      Return type
     * @return Method result
     * @throws Exception If the call fails
     */
    public abstract <T> T sync(final @NonNull Callable<T> function, final int timeout)
            throws Exception;

    /**
     * Call a method synchronously and return a future with
     * the result of the result
     *
     * @param method Method to be ran synchronously
     * @param <T>    Return type
     * @return Future completing with the result
     */
    public abstract <T> Future<T> callMethodSync(final @NonNull Callable<T> method);

    /**
     * Run a repeating synchronous task. If using a platform scheduler,
     * this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    public abstract PlotSquaredTask taskRepeat(
            @NonNull Runnable runnable,
            @NonNull TaskTime taskTime
    );

    /**
     * Run a repeating asynchronous task. This will never run on the
     * server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    public abstract PlotSquaredTask taskRepeatAsync(
            @NonNull Runnable runnable,
            @NonNull TaskTime taskTime
    );

    /**
     * Run an asynchronous task. This will never run on the server thread
     *
     * @param runnable Task to run
     */
    public abstract void taskAsync(@NonNull Runnable runnable);

    /**
     * Run a synchronous task. If using a platform scheduler, this is guaranteed
     * to run on the server thread
     *
     * @param runnable Task to run
     */
    public abstract void task(@NonNull Runnable runnable);

    /**
     * Run a synchronous task after a given delay.
     * If using a platform scheduler, this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public abstract void taskLater(@NonNull Runnable runnable, @NonNull TaskTime taskTime);

    /**
     * Run an asynchronous task after a given delay. This will never
     * run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public abstract void taskLaterAsync(@NonNull Runnable runnable, @NonNull TaskTime taskTime);

}
