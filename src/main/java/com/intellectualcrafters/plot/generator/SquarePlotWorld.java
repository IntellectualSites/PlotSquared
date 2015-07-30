package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;

public abstract class SquarePlotWorld extends GridPlotWorld {
    public static int PLOT_WIDTH_DEFAULT = 42;
    public static int ROAD_WIDTH_DEFAULT = 7;
    public static int ROAD_OFFSET_X_DEFAULT = 0;
    public static int ROAD_OFFSET__Z_DEFAULT = 0;
    public int PLOT_WIDTH;
    public int ROAD_WIDTH;
    public int ROAD_OFFSET_X;
    public int ROAD_OFFSET_Z;

    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        if (!config.contains("plot.height")) {
            PS.debug(" - &cConfiguration is null? (" + config.getCurrentPath() + ")");
        }
        this.PLOT_WIDTH = config.getInt("plot.size");
        this.ROAD_WIDTH = config.getInt("road.width");
        this.ROAD_OFFSET_X = config.getInt("road.offset.x");
        this.ROAD_OFFSET_Z = config.getInt("road.offset.z");
        this.SIZE = (short) (this.PLOT_WIDTH + this.ROAD_WIDTH);
    }

    public SquarePlotWorld(final String worldname) {
        super(worldname);
    }
}
