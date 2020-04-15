package com.plotsquared.core.command;

public class CmdInstance {
    public final Runnable command;
    public final long timestamp;

    public CmdInstance(final Runnable command) {
        this.command = command;
        timestamp = System.currentTimeMillis();
    }
}
