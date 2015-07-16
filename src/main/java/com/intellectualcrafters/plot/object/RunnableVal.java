package com.intellectualcrafters.plot.object;

public abstract class RunnableVal<T> implements Runnable {
    public T value;
    public abstract void run();
}
