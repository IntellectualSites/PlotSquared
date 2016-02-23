package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;

public abstract class GridPlotWorld extends PlotArea {

    public GridPlotWorld(String worldname, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        super(worldname, id, generator, min, max);
    }

    public short SIZE;
}
