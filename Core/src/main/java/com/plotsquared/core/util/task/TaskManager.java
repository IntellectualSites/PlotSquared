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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskManager {

    public static final HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    public static final HashMap<Integer, Integer> tasks = new HashMap<>();
    public static AtomicInteger index = new AtomicInteger(0);

    @Getter @Setter private static TaskManager implementation;

    public static int runTaskRepeat(Runnable runnable, int interval) {
        if (runnable != null) {
            if (getImplementation() == null) {
                throw new IllegalArgumentException("disabled");
            }
            return getImplementation().taskRepeat(runnable, interval);
        }
        return -1;
    }

    public static int runTaskRepeatAsync(Runnable runnable, int interval) {
        if (runnable != null) {
            if (getImplementation() == null) {
                throw new IllegalArgumentException("disabled");
            }
            return getImplementation().taskRepeatAsync(runnable, interval);
        }
        return -1;
    }

    public static void runTaskAsync(Runnable runnable) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().taskAsync(runnable);
        }
    }

    public static void runTask(Runnable runnable) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().task(runnable);
        }
    }

    /**
     * Run task later.
     *
     * @param runnable The task
     * @param delay    The delay in ticks
     */
    public static void runTaskLater(Runnable runnable, int delay) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().taskLater(runnable, delay);
        }
    }

    public static void runTaskLaterAsync(Runnable runnable, int delay) {
        if (runnable != null) {
            if (getImplementation() == null) {
                runnable.run();
                return;
            }
            getImplementation().taskLaterAsync(runnable, delay);
        }
    }

    /**
     * Break up a series of tasks so that they can run without lagging the server.
     *
     * @param objects
     * @param task
     * @param whenDone
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

    public abstract int taskRepeat(Runnable runnable, int interval);

    public abstract int taskRepeatAsync(Runnable runnable, int interval);

    public abstract void taskAsync(Runnable runnable);

    public abstract void task(Runnable runnable);

    public abstract void taskLater(Runnable runnable, int delay);

    public abstract void taskLaterAsync(Runnable runnable, int delay);

    public abstract void cancelTask(int task);
}
