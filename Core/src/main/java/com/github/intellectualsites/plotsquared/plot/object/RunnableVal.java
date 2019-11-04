package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

public abstract class RunnableVal<T> implements Runnable {
    public T value;

    public RunnableVal() {
    }

    public RunnableVal(T value) {
        this.value = value;
    }

    @Override public void run() {
        run(this.value);
    }

    public abstract void run(T value);
}
