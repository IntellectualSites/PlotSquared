package com.plotsquared.util;

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
