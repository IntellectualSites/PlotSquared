package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;

public abstract class GridPlotWorld extends PlotArea {

    public short SIZE;

    public GridPlotWorld(String worldName, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }
}
