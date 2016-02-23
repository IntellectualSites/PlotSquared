package com.intellectualcrafters.plot.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.RunnableVal;

public abstract class TaskManager {
    public static HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    
    public static AtomicInteger index = new AtomicInteger(0);
    public static HashMap<Integer, Integer> tasks = new HashMap<>();
    
    public static int runTaskRepeat(final Runnable r, final int interval) {
        if (r != null) {
            if (PS.get().TASK == null) {
                throw new IllegalArgumentException("disabled");
            }
            return PS.get().TASK.taskRepeat(r, interval);
        }
        return -1;
    }
    
    public static int runTaskRepeatAsync(final Runnable r, final int interval) {
        if (r != null) {
            if (PS.get().TASK == null) {
                throw new IllegalArgumentException("disabled");
            }
            return PS.get().TASK.taskRepeat(r, interval);
        }
        return -1;
    }
    
    public static void runTaskAsync(final Runnable r) {
        if (r != null) {
            if (PS.get().TASK == null) {
                r.run();
                return;
            }
            PS.get().TASK.taskAsync(r);
        }
    }
    
    public static void runTask(final Runnable r) {
        if (r != null) {
            if (PS.get().TASK == null) {
                r.run();
                return;
            }
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
            if (PS.get().TASK == null) {
                r.run();
                return;
            }
            PS.get().TASK.taskLater(r, delay);
        }
    }
    
    public static void runTaskLaterAsync(final Runnable r, final int delay) {
        if (r != null) {
            if (PS.get().TASK == null) {
                r.run();
                return;
            }
            PS.get().TASK.taskLaterAsync(r, delay);
        }
    }
    
    public abstract int taskRepeat(final Runnable r, final int interval);
    
    public abstract int taskRepeatAsync(final Runnable r, final int interval);
    
    public abstract void taskAsync(final Runnable r);
    
    public abstract void task(final Runnable r);
    
    public abstract void taskLater(final Runnable r, final int delay);
    
    public abstract void taskLaterAsync(final Runnable r, final int delay);
    
    public abstract void cancelTask(final int task);
    
    /**
     * Break up a series of tasks so that they can run without lagging the server
     * @param objects
     * @param task
     * @param whenDone
     */
    public static <T> void objectTask(Collection<T> objects, final RunnableVal<T> task, final Runnable whenDone) {
        final Iterator<T> iter = objects.iterator();
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                boolean hasNext;
                while ((hasNext = iter.hasNext()) && System.currentTimeMillis() - start < 5) {
                    task.value = iter.next();
                    task.run();
                }
                if (!hasNext) {
                    TaskManager.runTaskLater(whenDone, 1);
                } else {
                    TaskManager.runTaskLater(this, 1);
                }
            }
        });
    }
}
