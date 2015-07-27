package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PS;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TaskManager {
    public static HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    
    public static AtomicInteger index = new AtomicInteger(0);
    public static HashMap<Integer, Integer> tasks = new HashMap<>();

    public static int runTaskRepeat(final Runnable r, final int interval) {
        if (r != null) {
            return PS.get().TASK.taskRepeat(r, interval);
        }
        return -1;
    }

    public static void runTaskAsync(final Runnable r) {
        if (r != null) {
            PS.get().TASK.taskAsync(r);
        }
    }

    public static void runTask(final Runnable r) {
        if (r != null) {
            PS.get().TASK.task(r);
        }
    }

    /**
     * Run task later (delay in ticks)
     * @param r
     * @param delay
     */
    public static void runTaskLater(final Runnable r, final int delay) {
        if (r != null) {
            PS.get().TASK.taskLater(r, delay);
        }
    }

    public static void runTaskLaterAsync(final Runnable r, final int delay) {
        if (r != null) {
            PS.get().TASK.taskLaterAsync(r, delay);
        }
    }

    public abstract int taskRepeat(final Runnable r, int interval);

    public abstract void taskAsync(final Runnable r);

    public abstract void task(final Runnable r);

    public abstract void taskLater(final Runnable r, int delay);

    public abstract void taskLaterAsync(final Runnable r, int delay);

    public abstract void cancelTask(int task);
}
