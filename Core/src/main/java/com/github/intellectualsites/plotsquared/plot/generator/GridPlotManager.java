package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;

/**
 * A plot manager where plots tessellate in a grid formation symmetrical about x=z.
 */
public abstract class GridPlotManager extends PlotManager {
    public GridPlotManager(PlotArea plotArea) {
        super(plotArea);
    }
}
