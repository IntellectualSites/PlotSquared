package com.plotsquared.generator;

import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotManager;

/**
 * A plot manager where plots tessellate in a grid formation symmetrical about x=z.
 */
public abstract class GridPlotManager extends PlotManager {
    public GridPlotManager(PlotArea plotArea) {
        super(plotArea);
    }
}
