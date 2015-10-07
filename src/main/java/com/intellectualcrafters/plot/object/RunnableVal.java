package com.intellectualcrafters.plot.object;

public abstract class RunnableVal<T> implements Runnable {
    public T value;
    
    @Override
    public abstract void run();
    
    public void run(T value) {
        this.value = value;
        run();
    }
}
