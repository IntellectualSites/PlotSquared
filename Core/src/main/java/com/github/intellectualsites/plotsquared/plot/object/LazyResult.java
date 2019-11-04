package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

public abstract class LazyResult<T> {

    private T result;

    public T get() {
        return this.result;
    }

    public T getOrCreate() {
        if (this.result == null) {
            return this.result = create();
        }
        return this.result;
    }

    public abstract T create();
}
