package com.plotsquared.generator;

import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotId;
import org.jetbrains.annotations.NotNull;

public abstract class GridPlotWorld extends PlotArea {

    public short SIZE;

    public GridPlotWorld(String worldName, String id, @NotNull IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }
}
