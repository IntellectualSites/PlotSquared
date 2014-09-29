package com.intellectualcrafters.plot;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the PlotWorld class (obviously)
 * <br> - All existing PlotWorld instances should be kept in PlotMain (worlds variable)
 * <br> - The accessors and mutators are: 
 * <br> PlotMain.isPlotWorld(world)
 * <br> PlotMain.getPlotWorlds() or PlotMain.getPlotWorldsString() <- use this if you don't need to get world objects
 * <br> PlotMain.getWorldSettings(World) - get the PlotWorld class for a world
 * <br> 
 * <br> Also added is getWorldPlots(World) as the plots are now sorted per world
 * <br> 
 * <br> To get the world of a plot, you can use plot.world - (string)   or plot.getWorld() (world object)
 * <br> 
 * <br> All PlotWorld settings are per world in the settings.yml (these settings are automatically added when a world is loaded, either at startup or if a new world is created):
 * <br>  - You can find this in the WorldGenerator class (yeah, it's possibly not the best place, but it makes sure worlds are added to the settings.yml)
 * <br> 
 * <br> All new DEFAULT CONSTANTS should be static and be given a value
 * <br> All new variables should not be static and should not be given any values here, but rather in the WorldGenerator class
 *  
 **/
public class PlotWorld {

    public boolean AUTO_MERGE;
    public static boolean AUTO_MERGE_DEFAULT = false;
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
    public String PLOT_BIOME;
    /**
     * Default biome = FOREST
     */
    public static String PLOT_BIOME_DEFAULT = "FOREST";
    /**
     * PlotMain block
     */
    public String[] MAIN_BLOCK;
    /**
     * Default main block: 1
     */
    public static String[] MAIN_BLOCK_DEFAULT = new String[] { "1:0" };
    /**
     * Top blocks
     */
    public String[] TOP_BLOCK;
    /**
     * Default top blocks: {"2"}
     */
    public static String[] TOP_BLOCK_DEFAULT = new String[] { "2:0" };

    /**
     * Wall block
     */
    public String WALL_BLOCK;
    /**
     * Default wall block: 44
     */
    public static String WALL_BLOCK_DEFAULT = "44:0";

    /**
     * Wall filling
     */
    public String WALL_FILLING;
    /**
     * Default wall filling: 1
     */
    public static String WALL_FILLING_DEFAULT = "1:0";

    /**
     * Road stripes
     */
    public String ROAD_STRIPES;
    public boolean ROAD_STRIPES_ENABLED;
    public static boolean ROAD_STRIPES_ENABLED_DEFAULT = false;
    /**
     * Default road stripes: 35
     */
    public static String ROAD_STRIPES_DEFAULT = "98:0";
    //
    // /**
    // * Road stripes data value (byte)
    // */
    // public int ROAD_STRIPES_DATA;
    // /**
    // * Default road stripes data value: (byte) 0
    // */
    // public static int ROAD_STRIPES_DATA_DEFAULT = 0;
    //
    // /**
    // * Wall block data value (byte)
    // */
    // public int WALL_BLOCK_DATA;
    // /**
    // * Default wall block data value: (byte) 0
    // */
    // public static int WALL_BLOCK_DATA_DEFAULT = 0;
    //
    // /**
    // * Wall filling data value (byte)
    // */
    // public int WALL_FILLING_DATA;
    // /**
    // * Default wall filling data value: (byte) 0
    // */
    // public static int WALL_FILLING_DATA_DEFAULT = 0;
    /**
     * Road block
     */
    public String ROAD_BLOCK;
    /**
     * Default road block: 155
     */
    public static String ROAD_BLOCK_DEFAULT = "155:0";
    //
    // /**
    // * Road block data value (byte)
    // */
    // public int ROAD_BLOCK_DATA;
    // /**
    // * Default road block data value: (byte) 0
    // */
    // public static int ROAD_BLOCK_DATA_DEFAULT = 0;

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
    public List<String> SCHEMATICS = new ArrayList<>();

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
    public static String[] DEFAULT_FLAGS_DEFAULT = new String[] {};
}
