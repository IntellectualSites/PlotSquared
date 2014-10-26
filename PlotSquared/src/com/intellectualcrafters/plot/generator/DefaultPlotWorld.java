package com.intellectualcrafters.plot.generator;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import com.intellectualcrafters.plot.Configuration;
import com.intellectualcrafters.plot.ConfigurationNode;
import com.intellectualcrafters.plot.PlotBlock;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;

public class DefaultPlotWorld extends PlotWorld {

	/*
	 * These variables are set to ensure fast access to config settings Strings
	 * are used as little as possible to optimize math performance in many of
	 * the functions/algorithms
	 */

	/**
	 * Road Height
	 */
	public int ROAD_HEIGHT;
	/**
	 * Default Road Height: 64
	 */
	public static int ROAD_HEIGHT_DEFAULT = 64;

	/**
	 * plot height
	 */
	public int PLOT_HEIGHT;
	/**
	 * Default plot height: 64
	 */
	public static int PLOT_HEIGHT_DEFAULT = 64;

	/**
	 * Wall height
	 */
	public int WALL_HEIGHT;
	/**
	 * Default Wall Height: 64
	 */
	public static int WALL_HEIGHT_DEFAULT = 64;

	/**
	 * plot width
	 */
	public int PLOT_WIDTH;
	/**
	 * Default plot width: 32
	 */
	public static int PLOT_WIDTH_DEFAULT = 32;

	/**
	 * Road width
	 */
	public int ROAD_WIDTH;
	/**
	 * Default road width: 7
	 */
	public static int ROAD_WIDTH_DEFAULT = 7;

	/**
	 * Plot main block
	 */
	public PlotBlock[] MAIN_BLOCK;
	/**
	 * Default main block: 1
	 */
	public static PlotBlock[] MAIN_BLOCK_DEFAULT = new PlotBlock[] { new PlotBlock((short) 1, (byte) 0) };
	/**
	 * Top blocks
	 */
	public PlotBlock[] TOP_BLOCK;
	/**
	 * Default top blocks: {"2"}
	 */
	public static PlotBlock[] TOP_BLOCK_DEFAULT = new PlotBlock[] { new PlotBlock((short) 2, (byte) 0) };

	/**
	 * Wall block
	 */
	public PlotBlock WALL_BLOCK;
	/**
	 * Default wall block: 44
	 */
	public static PlotBlock WALL_BLOCK_DEFAULT = new PlotBlock((short) 44, (byte) 0);

	/**
	 * Wall filling
	 */
	public PlotBlock WALL_FILLING;
	/**
	 * Default wall filling: 1
	 */
	public static PlotBlock WALL_FILLING_DEFAULT = new PlotBlock((short) 1, (byte) 0);

	/**
	 * Road stripes
	 */
	public PlotBlock ROAD_STRIPES;
	/**
	 * Default road stripes: 35
	 */
	public static PlotBlock ROAD_STRIPES_DEFAULT = new PlotBlock((short) 98, (byte) 0);
	/**
	 * enable road stripes
	 */
	public boolean ROAD_STRIPES_ENABLED;
	public static boolean ROAD_STRIPES_ENABLED_DEFAULT = false;
	/**
	 * Road block
	 */
	public PlotBlock ROAD_BLOCK;
	/**
	 * Default road block: 155
	 */
	public static PlotBlock ROAD_BLOCK_DEFAULT = new PlotBlock((short) 155, (byte) 0);

	/*
	 * Here we are just calling the super method, nothing special
	 */
	public DefaultPlotWorld(String worldname) {
		super(worldname);
	}

	/**
	 * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED
	 * FOR INITIAL SETUP
	 * 
	 * Set the last boolean to false if you do not require a specific config
	 * node to be set while using the setup command - this may be useful if a
	 * config value can be changed at a later date, and has no impact on the
	 * actual world generation
	 */
	@Override
	public ConfigurationNode[] getSettingNodes() {
		// TODO return a set of configuration nodes (used for setup command)
		return new ConfigurationNode[] {
				new ConfigurationNode("plot.height", DefaultPlotWorld.PLOT_HEIGHT_DEFAULT, "Plot height", Configuration.INTEGER, true),
				new ConfigurationNode("plot.size", DefaultPlotWorld.PLOT_WIDTH_DEFAULT, "Plot width", Configuration.INTEGER, true),
				new ConfigurationNode("plot.filling", DefaultPlotWorld.MAIN_BLOCK_DEFAULT, "Plot block", Configuration.BLOCKLIST, true),
				new ConfigurationNode("plot.floor", DefaultPlotWorld.TOP_BLOCK_DEFAULT, "Plot floor block", Configuration.BLOCKLIST, true),
				new ConfigurationNode("wall.block", DefaultPlotWorld.WALL_BLOCK_DEFAULT, "Top wall block", Configuration.BLOCK, true),
				new ConfigurationNode("road.width", DefaultPlotWorld.ROAD_WIDTH_DEFAULT, "Road width", Configuration.INTEGER, true),
				new ConfigurationNode("road.height", DefaultPlotWorld.ROAD_HEIGHT_DEFAULT, "Road height", Configuration.INTEGER, true),
				new ConfigurationNode("road.enable_stripes", DefaultPlotWorld.ROAD_STRIPES_ENABLED_DEFAULT, "Enable road stripes", Configuration.BOOLEAN, true),
				new ConfigurationNode("road.block", DefaultPlotWorld.ROAD_BLOCK_DEFAULT, "Road block", Configuration.BLOCK, true),
				new ConfigurationNode("road.stripes", DefaultPlotWorld.ROAD_STRIPES_DEFAULT, "Road stripe block", Configuration.BLOCK, true),
				new ConfigurationNode("wall.filling", DefaultPlotWorld.WALL_FILLING_DEFAULT, "Wall filling block", Configuration.BLOCK, true),
				new ConfigurationNode("wall.height", DefaultPlotWorld.WALL_HEIGHT_DEFAULT, "Wall height", Configuration.INTEGER, true), };
	}

	/**
	 * This method is called when a world loads. Make sure you set all your
	 * constants here. You are provided with the configuration section for that
	 * specific world.
	 */
	@Override
	public void loadConfiguration(ConfigurationSection config) {
		this.PLOT_HEIGHT = config.getInt("plot.height");
		
		if (!config.contains("plot.height")) {
		    PlotMain.sendConsoleSenderMessage(" - &Configuration is null? ("+config.getCurrentPath()+")");
		}
		
		this.PLOT_WIDTH = config.getInt("plot.size");
		this.MAIN_BLOCK =
				(PlotBlock[]) Configuration.BLOCKLIST.parseString(StringUtils.join(config.getStringList("plot.filling"), ','));
		this.TOP_BLOCK =
				(PlotBlock[]) Configuration.BLOCKLIST.parseString(StringUtils.join(config.getStringList("plot.floor"), ','));
		this.WALL_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.block"));
		this.ROAD_WIDTH = config.getInt("road.width");
		this.ROAD_HEIGHT = config.getInt("road.height");
		this.ROAD_STRIPES_ENABLED = config.getBoolean("road.enable_stripes");
		this.ROAD_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("road.block"));
		this.ROAD_STRIPES = (PlotBlock) Configuration.BLOCK.parseString(config.getString("road.stripes"));
		this.WALL_FILLING = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.filling"));
		this.WALL_HEIGHT = config.getInt("wall.height");

	}
}
