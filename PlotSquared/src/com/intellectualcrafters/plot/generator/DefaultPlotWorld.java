package com.intellectualcrafters.plot.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import com.intellectualcrafters.plot.Configuration.*;
import com.intellectualcrafters.plot.Configuration;
import com.intellectualcrafters.plot.ConfigurationNode;
import com.intellectualcrafters.plot.Flag;
import com.intellectualcrafters.plot.PlotBlock;
import com.intellectualcrafters.plot.PlotWorld;

public class DefaultPlotWorld extends PlotWorld {

    /*
     * These variables are set to ensure fast access to config settings
     * Strings are used as little as possible to optimize math performance in many of the functions/algorithms
     * 
     */
    
    public boolean AUTO_MERGE;
    public static boolean AUTO_MERGE_DEFAULT = false;
    public boolean MOB_SPAWNING;
    public static boolean MOB_SPAWNING_DEFAULT = false;
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
     * Plot biome
     */
    public Biome PLOT_BIOME;
    /**
     * Default biome = FOREST
     */
    public static Biome PLOT_BIOME_DEFAULT = Biome.FOREST;
    /**
     * PlotMain block
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
    public static String WALL_BLOCK_DEFAULT = "44:0";

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

    /**
     * plot chat?
     */
    public boolean PLOT_CHAT;
    /**
     * Default plot chat: true
     */
    public static boolean PLOT_CHAT_DEFAULT = false;

    /**
     * Blocks available in /p set
     */
    public static ArrayList<Material> BLOCKS = new ArrayList<Material>();

    /**
     * schematic on claim
     */
    public boolean SCHEMATIC_ON_CLAIM;
    /**
     * Default schematic on claim: false
     */
    public static boolean SCHEMATIC_ON_CLAIM_DEFAULT = false;
    public boolean SCHEMATIC_CLAIM_SPECIFY = false;
    public List<String> SCHEMATICS = new ArrayList<String>();

    /**
     * schematic file
     */
    public String SCHEMATIC_FILE;
    /**
     * Default schematic file: 'null'
     */
    public static String SCHEMATIC_FILE_DEFAULT = "null";
    /**
     * default flags
     */
    public Flag[] DEFAULT_FLAGS;
    /**
     * Default default flags
     */
    public static Flag[] DEFAULT_FLAGS_DEFAULT = new Flag[] {};

    public boolean USE_ECONOMY;
    public static boolean USE_ECONOMY_DEFAULT = false;
    
    public double PLOT_PRICE;
    public static double PLOT_PRICE_DEFAULT = 100;
    
    public double MERGE_PRICE;
    public static double MERGE_PRICE_DEFAULT = 100;
    
    
    /*
     * Here we are just calling the super method, nothing special
     */
    public DefaultPlotWorld(String worldname) {
        super(worldname);
    }

    /*
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP
     * 
     * Set the last boolean to false if you do not require a specific config node to be set while using the setup command
     *  - this may be useful if a config value can be changed at a later date, and has no impact on the actual world generation
     * 
     */
    @Override
    public ConfigurationNode[] getSettingNodes() {
        // TODO return a set of configuration nodes (used for setup command)
        return
            new ConfigurationNode[] {
                new ConfigurationNode("natural_mob_spawning", MOB_SPAWNING, "Enable mob spawning", Configuration.BOOLEAN, false),
                new ConfigurationNode("plot.auto_merge", AUTO_MERGE, "Enable Auto plot merging", Configuration.BOOLEAN, false),
                new ConfigurationNode("plot.height", PLOT_HEIGHT, "Plot height", Configuration.INTEGER, true),
                new ConfigurationNode("plot.width", PLOT_WIDTH, "Plot width", Configuration.INTEGER, true),
                new ConfigurationNode("plot.biome", PLOT_BIOME, "Plot biome", Configuration.BIOME, true),
                new ConfigurationNode("plot.filling", MAIN_BLOCK, "Plot block", Configuration.BLOCKLIST, true),
                new ConfigurationNode("plot.floor", TOP_BLOCK, "Plot floor block", Configuration.BLOCKLIST, true),
                new ConfigurationNode("wall.block", WALL_BLOCK, "Top wall block", Configuration.BLOCK, true),
                new ConfigurationNode("road.width", ROAD_WIDTH, "Road width", Configuration.INTEGER, true),
                new ConfigurationNode("road.height", ROAD_HEIGHT, "Road height", Configuration.INTEGER, true),
                new ConfigurationNode("road.enable_stripes", ROAD_STRIPES_ENABLED, "Enable road stripes", Configuration.BOOLEAN, true),
                new ConfigurationNode("road.block", ROAD_BLOCK, "Road block", Configuration.BLOCK, true),
                new ConfigurationNode("road.stripes", ROAD_STRIPES, "Road stripe block", Configuration.BLOCK, true),
                new ConfigurationNode("wall.filling", WALL_FILLING, "Wall filling block", Configuration.BLOCK, true),
                new ConfigurationNode("wall.height", WALL_HEIGHT, "Wall height", Configuration.INTEGER, true),
                new ConfigurationNode("schematic.on_claim", SCHEMATIC_ON_CLAIM, "Enable schematic paste on claim", Configuration.BOOLEAN, false),
                new ConfigurationNode("schematic.file", SCHEMATIC_FILE, "Schematic file directory", Configuration.STRING, false),
                new ConfigurationNode("schematic.specify_on_claim", SCHEMATIC_CLAIM_SPECIFY, "Enable specifying schematics on claim", Configuration.BOOLEAN, false),
                new ConfigurationNode("schematic.schematics", SCHEMATICS, "List of schematic paths", Configuration.STRINGLIST, false),
                new ConfigurationNode("economy.use", USE_ECONOMY, "Enable economy features", Configuration.BOOLEAN, false),
                new ConfigurationNode("economy.prices.claim", PLOT_PRICE, "Plot claim price", Configuration.DOUBLE, false),
                new ConfigurationNode("economy.prices.merge", MERGE_PRICE, "Plot merge price", Configuration.DOUBLE, false),
                new ConfigurationNode("chat.enabled", PLOT_CHAT, "Enable plot chat", Configuration.BOOLEAN, false)
            };
    }

    /*
     * This method is called when a world loads. Make sure you set all your constants here.
     * You are provided with the configuration section for that specific world.
     */
    @Override
    public void loadConfiguration(ConfigurationSection config) {
        this.MOB_SPAWNING = config.getBoolean("natural_mob_spawning");
        this.AUTO_MERGE = config.getBoolean("plot.auto_merge");
        this.PLOT_HEIGHT = config.getInt("plot.height");
        this.PLOT_WIDTH = config.getInt("plot.width");
        this.PLOT_BIOME = (Biome) Configuration.BIOME.parseString(config.getString("plot.biome"));
        this.MAIN_BLOCK = (PlotBlock[]) Configuration.BLOCKLIST.parseString(StringUtils.join(config.getStringList("plot.filling"),','));
        this.TOP_BLOCK = (PlotBlock[]) Configuration.BLOCKLIST.parseString(StringUtils.join(config.getStringList("plot.floor"),','));
        this.WALL_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.block"));
        this.ROAD_WIDTH = config.getInt("road.width");
        this.ROAD_HEIGHT = config.getInt("road.height");
        this.ROAD_STRIPES_ENABLED = config.getBoolean("road.enable_stripes");
        this.ROAD_BLOCK = (PlotBlock) Configuration.BLOCK.parseString(config.getString("road.block"));
        this.ROAD_STRIPES = (PlotBlock) Configuration.BLOCK.parseString(config.getString("road.stripes"));
        this.WALL_FILLING = (PlotBlock) Configuration.BLOCK.parseString(config.getString("wall.filling"));
        this.WALL_HEIGHT = config.getInt("wall.height");
        this.SCHEMATIC_ON_CLAIM = config.getBoolean("schematic.on_claim");
        this.SCHEMATIC_FILE = config.getString("schematic.file");
        this.SCHEMATIC_CLAIM_SPECIFY = config.getBoolean("schematic.specify_on_claim");
        this.SCHEMATICS = config.getStringList("schematic.schematics");
        this.USE_ECONOMY = config.getBoolean("economy.use");
        this.PLOT_PRICE = config.getDouble("economy.prices.claim");
        this.MERGE_PRICE = config.getDouble("economy.prices.merge");
        this.PLOT_CHAT = config.getBoolean("chat.enabled");
    }
}
 