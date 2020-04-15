package com.plotsquared.core.plot;

public abstract class PlotFilter {
    public boolean allowsArea(final PlotArea area) {
        return true;
    }

    public boolean allowsPlot(final Plot plot) {
        return true;
    }
}
