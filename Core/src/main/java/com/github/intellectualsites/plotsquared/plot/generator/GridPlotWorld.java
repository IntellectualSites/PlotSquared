package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import org.jetbrains.annotations.NotNull;

public abstract class GridPlotWorld extends PlotArea {

    public short SIZE;

    public GridPlotWorld(String worldName, String id, @NotNull IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }
}
