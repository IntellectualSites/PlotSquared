package com.plotsquared.core.util.task;

/**
 * A runnable that can be yielded.
 * If {@link #yield()} is invoked, {@link #run()} will be called
 * on the next tick again. Implementations need to save their state
 * correctly.
 */
public interface YieldRunnable extends Runnable {

    /**
     * Runs the {@link #run()} method again on the next tick.
     */
    default void yield() {
        TaskManager.runTaskLater(this, TaskTime.ticks(1L));
    }
}
