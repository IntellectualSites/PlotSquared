package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

public class CmdInstance {
    public final Runnable command;
    public final long timestamp;

    public CmdInstance(final Runnable command) {
        this.command = command;
        timestamp = System.currentTimeMillis();
    }
}
