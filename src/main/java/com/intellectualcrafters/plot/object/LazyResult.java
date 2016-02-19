package com.intellectualcrafters.plot.object;

public abstract class LazyResult<T> {
    private T result;
    
    public T get() {
        return result;
    }

    public T getOrCreate() {
        if (this.result == null) {
            return (this.result = create());
        }
        return result;
    }
    
    public abstract T create();
}
