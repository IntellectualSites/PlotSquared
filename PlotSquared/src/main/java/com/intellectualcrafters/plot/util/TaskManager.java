package com.intellectualcrafters.plot.util;

import java.util.HashSet;

import com.intellectualcrafters.plot.PlotSquared;

public abstract class TaskManager {
    
    public HashSet<String> TELEPORT_QUEUE = new HashSet<>(); 
    
    public abstract void taskRepeat(final Runnable r, int interval);
    
    public abstract void taskAsync(final Runnable r);
    
    public abstract void task(final Runnable r);
    
    public abstract void taskLater(final Runnable r, int delay);
    
    public abstract void taskLaterAsync(final Runnable r, int delay);
    
    public static void runTaskRepeat(final Runnable r, int interval) {
        if (r != null)
            PlotSquared.TASK.taskRepeat(r, interval);
    }
    
    public static void runTaskAsync(final Runnable r) {
        if (r != null)
            PlotSquared.TASK.taskAsync(r);
    }
    
    public static void runTask(final Runnable r) {
        if (r != null)
            PlotSquared.TASK.task(r);
    }
    
    public static void runTaskLater(final Runnable r, int delay) {
        if (r != null)
            PlotSquared.TASK.taskLater(r, delay);
    }
    
    public static void runTaskLaterAsync(final Runnable r, int delay) {
        if (r != null)
            PlotSquared.TASK.taskLaterAsync(r, delay);
    }
}
