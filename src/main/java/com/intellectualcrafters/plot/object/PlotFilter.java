package com.intellectualcrafters.plot.object;

public abstract class PlotFilter {
    public boolean allowsWorld(String world) {
        return true;
    }
    public boolean allowsPlot(Plot plot) {
        return true;
    }
}
