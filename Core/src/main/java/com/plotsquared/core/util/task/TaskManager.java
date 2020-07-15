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
package com.plotsquared.core.util.task;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.RuntimeExceptionRunnableVal;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskManager {

    public static final HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    public static final HashMap<Integer, PlotSquaredTask> tasks = new HashMap<>();
    public static AtomicInteger index = new AtomicInteger(0);

    @Getter @Setter private static TaskManager implementation;

    /**
     * Run a repeating synchronous task. If using a platform scheduler,
     * this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    @Nonnull public static PlotSquaredTask runTaskRepeat(@Nullable final Runnable runnable,
                                                         @Nonnull final TaskTime taskTime) {
        if (runnable != null) {
            if (getImplementation() == null) {
                throw new IllegalArgumentException("disabled");
            }
            return getImplementation().taskRepeat(runnable, taskTime);
        }
        return PlotSquaredTask.nullTask();
    }

    /**
     * Run a repeating asynchronous task. This will never run on the
     * server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    @Nonnull public static PlotSquaredTask runTaskRepeatAsync(@Nullable final Runnable runnable,
                                                              @NotNull final TaskTime taskTime) {
        if (runnable != null) {
            if (getImplementation() == null) {
                throw new IllegalArgumentException("disabled");
            }
            return getImplementation().taskRepeatAsync(runnable, taskTime);
        }
        return PlotSquaredTask.nullTask();
    }

    /**
     * Run an asynchronous task. This will never run on the server thread
     *
     * @param runnable Task to run
     */
    public static void runTaskAsync(@Nullable final Runnable runnable) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().taskAsync(runnable);
        }
    }

    /**
     * Run a synchronous task. If using a platform scheduler, this is guaranteed
     * to run on the server thread
     *
     * @param runnable Task to run
     */
    public static void runTask(@Nullable final Runnable runnable) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().task(runnable);
        }
    }

    /**
     * Run a synchronous task after a given delay.
     * If using a platform scheduler, this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public static void runTaskLater(@Nullable final Runnable runnable,
                                    @Nonnull final TaskTime taskTime) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().taskLater(runnable, taskTime);
        }
    }

    /**
     * Run an asynchronous task after a given delay. This will never
     * run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public static void runTaskLaterAsync(@Nullable final Runnable runnable,
                                         @Nonnull final TaskTime taskTime) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().taskLaterAsync(runnable, taskTime);
        }
    }

    /**
     * Break up a series of tasks so that they can run without lagging the server.
     */
    public static <T> void objectTask(Collection<T> objects, final RunnableVal<T> task,
        final Runnable whenDone) {
        final Iterator<T> iterator = objects.iterator();
        TaskManager.runTask(new ObjectTaskRunnable<>(iterator, task, whenDone));
    }

    public <T> T sync(final RunnableVal<T> function) {
        return sync(function, Integer.MAX_VALUE);
    }

    public <T> T sync(final RunnableVal<T> function, int timeout) {
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            function.run();
            return function.value;
        }
        final AtomicBoolean running = new AtomicBoolean(true);
        final RuntimeExceptionRunnableVal<T> run =
            new RuntimeExceptionRunnableVal<>(function, running);
        TaskManager.getImplementation().task(run);
        try {
            synchronized (function) {
                while (running.get()) {
                    function.wait(timeout);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (run.value != null) {
            throw run.value;
        }
        return function.value;
    }

    /**
     * Run a repeating synchronous task. If using a platform scheduler,
     * this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    public abstract PlotSquaredTask taskRepeat(@Nonnull Runnable runnable,
                                               @Nonnull TaskTime taskTime);

    /**
     * Run a repeating asynchronous task. This will never run on the
     * server thread
     *
     * @param runnable Task to run
     * @param taskTime Task interval
     * @return Created task object, can be used to cancel the task
     */
    public abstract PlotSquaredTask taskRepeatAsync(@Nonnull Runnable runnable,
                                                    @Nonnull TaskTime taskTime);

    /**
     * Run an asynchronous task. This will never run on the server thread
     *
     * @param runnable Task to run
     */
    public abstract void taskAsync(@Nonnull Runnable runnable);

    /**
     * Run a synchronous task. If using a platform scheduler, this is guaranteed
     * to run on the server thread
     *
     * @param runnable Task to run
     */
    public abstract void task(@Nonnull Runnable runnable);

    /**
     * Run a synchronous task after a given delay.
     * If using a platform scheduler, this is guaranteed to run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public abstract void taskLater(@Nonnull Runnable runnable,
                                   @Nonnull TaskTime taskTime);

    /**
     * Run an asynchronous task after a given delay. This will never
     * run on the server thread
     *
     * @param runnable Task to run
     * @param taskTime Task delay
     */
    public abstract void taskLaterAsync(@Nonnull Runnable runnable,
                                        @Nonnull TaskTime taskTime);

}
