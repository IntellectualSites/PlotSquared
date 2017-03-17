package com.intellectualcrafters.plot.object.worlds;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;

public class SinglePlotArea extends PlotArea {
    public SinglePlotArea(String worldName, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }

    @Override
    public void loadConfiguration(ConfigurationSection config) {

    }

    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[0];
    }
}
