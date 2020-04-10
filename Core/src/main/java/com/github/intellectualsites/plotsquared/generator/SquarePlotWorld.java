package com.github.intellectualsites.plotsquared.generator;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import org.jetbrains.annotations.NotNull;

public abstract class SquarePlotWorld extends GridPlotWorld {

    public int PLOT_WIDTH = 42;
    public int ROAD_WIDTH = 7;
    public int ROAD_OFFSET_X = 0;
    public int ROAD_OFFSET_Z = 0;

    public SquarePlotWorld(String worldName, String id, @NotNull IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }

    @Override public void loadConfiguration(ConfigurationSection config) {
        if (!config.contains("plot.height")) {
            PlotSquared.debug(" - &cConfiguration is null? (" + config.getCurrentPath() + ')');
        }
        this.PLOT_WIDTH = config.getInt("plot.size");
        this.ROAD_WIDTH = config.getInt("road.width");
        this.ROAD_OFFSET_X = config.getInt("road.offset.x");
        this.ROAD_OFFSET_Z = config.getInt("road.offset.z");
        this.SIZE = (short) (this.PLOT_WIDTH + this.ROAD_WIDTH);
    }
}
