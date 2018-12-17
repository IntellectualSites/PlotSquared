package com.github.intellectualsites.plotsquared.plot.object;

public abstract class LazyBlock {

    public abstract StringPlotBlock getPlotBlock();

    public String getId() {
        return getPlotBlock().getItemId();
    }
}
