package com.plotsquared.core.queue;

import com.plotsquared.core.util.task.PlotSquaredTask;

public abstract class ChunkCoordinator implements PlotSquaredTask {

    @Override public abstract void runTask();

    @Override public boolean isCancelled() {
        return false;
    }

    @Override public void cancel() {
        // Do nothing
    }

    public abstract void start();

    public abstract int getRemainingChunks();

    public abstract int getTotalChunks();
}
