package com.github.intellectualsites.plotsquared.plot.logger;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

public class DelegateLogger implements ILogger {

    private final ILogger parent;

    public DelegateLogger(ILogger parent) {
        this.parent = parent;
    }

    public ILogger getParent() {
        return parent;
    }

    @Override public void log(String message) {
        parent.log(message);
    }
}
