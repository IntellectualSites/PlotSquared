package com.intellectualcrafters.plot.util;

import java.util.HashSet;

import com.intellectualcrafters.plot.PlotSquared;

public abstract class TaskManager {
    public static HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    
    public abstract int taskRepeat(final Runnable r, int interval);
    
    public abstract int taskAsync(final Runnable r);
    
    public abstract int task(final Runnable r);
    
    public abstract int taskLater(final Runnable r, int delay);
    
    public abstract int taskLaterAsync(final Runnable r, int delay);
    
    public abstract void cancelTask(int task);
    
    public static int runTaskRepeat(final Runnable r, final int interval) {
        if (r != null) {
            return PlotSquared.TASK.taskRepeat(r, interval);
        }
        return -1;
    }
    
    public static int runTaskAsync(final Runnable r) {
        if (r != null) {
            return PlotSquared.TASK.taskAsync(r);
        }
        return -1;
    }
    
    public static int runTask(final Runnable r) {
        if (r != null) {
            return PlotSquared.TASK.task(r);
        }
        return -1;
    }
    
    public static int runTaskLater(final Runnable r, final int delay) {
        if (r != null) {
            return PlotSquared.TASK.taskLater(r, delay);
        }
        return -1;
    }
    
    public static int runTaskLaterAsync(final Runnable r, final int delay) {
        if (r != null) {
            return PlotSquared.TASK.taskLaterAsync(r, delay);
        }
        return -1;
    }
}
