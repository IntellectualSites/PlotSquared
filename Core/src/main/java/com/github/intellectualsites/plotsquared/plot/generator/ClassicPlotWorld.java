package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

public abstract class ClassicPlotWorld extends SquarePlotWorld {

    public int ROAD_HEIGHT = 64;
    public int PLOT_HEIGHT = 64;
    public int WALL_HEIGHT = 64;
    public PlotBlock[] MAIN_BLOCK = new PlotBlock[] {PlotBlock.get((short) 1, (byte) 0)};
    public PlotBlock[] TOP_BLOCK = new PlotBlock[] {PlotBlock.get((short) 2, (byte) 0)};
    public PlotBlock WALL_BLOCK = PlotBlock.get((short) 44, (byte) 0);
    public PlotBlock CLAIMED_WALL_BLOCK = PlotBlock.get((short) 44, (byte) 1);
    public PlotBlock WALL_FILLING = PlotBlock.get((short) 1, (byte) 0);
    public PlotBlock ROAD_BLOCK = PlotBlock.get((short) 155, (byte) 0);
    public boolean PLOT_BEDROCK = true;

    public ClassicPlotWorld(String worldName, String id, IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }

    /**
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP.
     * <p>
     * <p>Set the last boolean to false if you do not check a specific config node to be set while using the setup
     * command - this may be useful if a config value can be changed at a later date, and has no impact on the actual
     * world generation</p>
     */
    @Override public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
            new ConfigurationNode("plot.height", this.PLOT_HEIGHT, "Plot height",
                Configuration.INTEGER),
            new ConfigurationNode("plot.size", this.PLOT_WIDTH, "Plot width",
                Configuration.INTEGER),
            new ConfigurationNode("plot.filling", this.MAIN_BLOCK, "Plot block",
                Configuration.BLOCKLIST),
            new ConfigurationNode("plot.floor", this.TOP_BLOCK, "Plot floor block",
                Configuration.BLOCKLIST),
            new ConfigurationNode("wall.block", this.WALL_BLOCK, "Top wall block",
                Configuration.BLOCK),
            new ConfigurationNode("wall.block_claimed", this.CLAIMED_WALL_BLOCK,
                "Wall block (claimed)", Configuration.BLOCK),
            new ConfigurationNode("road.width", this.ROAD_WIDTH, "Road width",
                Configuration.INTEGER),
            new ConfigurationNode("road.height", this.ROAD_HEIGHT, "Road height",
                Configuration.INTEGER),
            new ConfigurationNode("road.block", this.ROAD_BLOCK, "Road block", Configuration.BLOCK),
            new ConfigurationNode("wall.filling", this.WALL_FILLING, "Wall filling block",
                Configuration.BLOCK),
            new ConfigurationNode("wall.height", this.WALL_HEIGHT, "Wall height",
                Configuration.INTEGER),
            new ConfigurationNode("plot.bedrock", this.PLOT_BEDROCK, "Plot bedrock generation",
                Configuration.BOOLEAN)};
    }

    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override public void loadConfiguration(ConfigurationSection config) {
        super.loadConfiguration(config);
        this.PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        this.PLOT_HEIGHT = Math.min(255, config.getInt("plot.height"));
        this.MAIN_BLOCK = Configuration.BLOCKLIST
            .parseString(StringMan.join(config.getStringList("plot.filling"), ','));
        this.TOP_BLOCK = Configuration.BLOCKLIST
            .parseString(StringMan.join(config.getStringList("plot.floor"), ','));
        this.WALL_BLOCK = Configuration.BLOCK.parseString(config.getString("wall.block"));
        this.ROAD_HEIGHT = Math.min(255, config.getInt("road.height"));
        this.ROAD_BLOCK = Configuration.BLOCK.parseString(config.getString("road.block"));
        this.WALL_FILLING = Configuration.BLOCK.parseString(config.getString("wall.filling"));
        this.WALL_HEIGHT = Math.min(254, config.getInt("wall.height"));
        this.CLAIMED_WALL_BLOCK =
            Configuration.BLOCK.parseString(config.getString("wall.block_claimed"));
    }
}
