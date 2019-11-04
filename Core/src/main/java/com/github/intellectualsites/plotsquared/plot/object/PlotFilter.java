package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

public abstract class PlotFilter {
    public boolean allowsArea(final PlotArea area) {
        return true;
    }

    public boolean allowsPlot(final Plot plot) {
        return true;
    }
}
