package com.intellectualcrafters.plot.generator;

import org.bukkit.configuration.ConfigurationSection;

import com.intellectualcrafters.plot.PlotSquared;

public abstract class SquarePlotWorld extends GridPlotWorld {
    
    public static int PLOT_WIDTH_DEFAULT = 42;
    public static int ROAD_WIDTH_DEFAULT = 7;
    
    public int PLOT_WIDTH;
    public int ROAD_WIDTH;
    
    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        if (!config.contains("plot.height")) {
            PlotSquared.sendConsoleSenderMessage(" - &cConfiguration is null? (" + config.getCurrentPath() + ")");
        }
        this.PLOT_WIDTH = config.getInt("plot.size");
        this.ROAD_WIDTH = config.getInt("road.width");
        this.SIZE = (short) (this.PLOT_WIDTH + this.ROAD_WIDTH);
    }
    
    public SquarePlotWorld(String worldname) {
        super(worldname);
    }
}
