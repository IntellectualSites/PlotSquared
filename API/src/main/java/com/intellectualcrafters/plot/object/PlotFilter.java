package com.intellectualcrafters.plot.object;

public abstract class PlotFilter {
    public boolean allowsWorld(final String world) {
        return true;
    }
    
    public boolean allowsPlot(final Plot plot) {
        return true;
    }
}
