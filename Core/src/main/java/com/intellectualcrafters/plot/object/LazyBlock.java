package com.intellectualcrafters.plot.object;

public abstract class LazyBlock {

    public abstract PlotBlock getPlotBlock();

    public int getId() {
        return getPlotBlock().id;
    }
}
