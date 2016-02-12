package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.StringMan;

public abstract class ClassicPlotWorld extends SquarePlotWorld {
    
    public ClassicPlotWorld(String worldname, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        super(worldname, id, generator, min, max);
    }

    public int ROAD_HEIGHT = 64;
    public int PLOT_HEIGHT = 64;
    public int WALL_HEIGHT = 64;
    public PlotBlock[] MAIN_BLOCK = new PlotBlock[] { new PlotBlock((short) 1, (byte) 0) };
    public PlotBlock[] TOP_BLOCK = new PlotBlock[] { new PlotBlock((short) 2, (byte) 0) };
    public PlotBlock WALL_BLOCK = new PlotBlock((short) 44, (byte) 0);
    public PlotBlock CLAIMED_WALL_BLOCK = new PlotBlock((short) 44, (byte) 1);
    public PlotBlock WALL_FILLING = new PlotBlock((short) 1, (byte) 0);
    public PlotBlock ROAD_BLOCK = new PlotBlock((short) 155, (byte) 0);
    public boolean PLOT_BEDROCK = true;
    
    /**
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP
     *
     * Set the last boolean to false if you do not require a specific config node to be set while using the setup
     * command - this may be useful if a config value can be changed at a later date, and has no impact on the actual
     * world generation
     */
    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
        new ConfigurationNode("plot.height", PLOT_HEIGHT, "Plot height", Configuration.INTEGER, true),
        new ConfigurationNode("plot.size", PLOT_WIDTH, "Plot width", Configuration.INTEGER, true),
        new ConfigurationNode("plot.filling", MAIN_BLOCK, "Plot block", Configuration.BLOCKLIST, true),
        new ConfigurationNode("plot.floor", TOP_BLOCK, "Plot floor block", Configuration.BLOCKLIST, true),
        new ConfigurationNode("wall.block", WALL_BLOCK, "Top wall block", Configuration.BLOCK, true),
        new ConfigurationNode("wall.block_claimed", CLAIMED_WALL_BLOCK, "Wall block (claimed)", Configuration.BLOCK, true),
        new ConfigurationNode("road.width", ROAD_WIDTH, "Road width", Configuration.INTEGER, true),
        new ConfigurationNode("road.height", ROAD_HEIGHT, "Road height", Configuration.INTEGER, true),
        new ConfigurationNode("road.block", ROAD_BLOCK, "Road block", Configuration.BLOCK, true),
        new ConfigurationNode("wall.filling", WALL_FILLING, "Wall filling block", Configuration.BLOCK, true),
        new ConfigurationNode("wall.height", WALL_HEIGHT, "Wall height", Configuration.INTEGER, true),
        new ConfigurationNode("plot.bedrock", PLOT_BEDROCK, "Plot bedrock generation", Configuration.BOOLEAN, true) };
    }
    
    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        super.loadConfiguration(config);
        PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        PLOT_HEIGHT = Math.min(255, config.getInt("plot.height"));
        MAIN_BLOCK = Configuration.BLOCKLIST.parseString(StringMan.join(config.getStringList("plot.filling"), ','));
        TOP_BLOCK = Configuration.BLOCKLIST.parseString(StringMan.join(config.getStringList("plot.floor"), ','));
        WALL_BLOCK = Configuration.BLOCK.parseString(config.getString("wall.block"));
        ROAD_HEIGHT = Math.min(255, config.getInt("road.height"));
        ROAD_BLOCK = Configuration.BLOCK.parseString(config.getString("road.block"));
        WALL_FILLING = Configuration.BLOCK.parseString(config.getString("wall.filling"));
        WALL_HEIGHT = Math.min(254, config.getInt("wall.height"));
        CLAIMED_WALL_BLOCK = Configuration.BLOCK.parseString(config.getString("wall.block_claimed"));
    }
}
