package com.intellectualcrafters.plot;

import org.bukkit.Material;

import java.util.ArrayList;

public class PlotWorld {
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
    public static String[] MAIN_BLOCK_DEFAULT = new String[] {"1"};
    /**
     * Top blocks
     */
    public String[] TOP_BLOCK;
    /**
     * Default top blocks: {"2"}
     */
    public static String[] TOP_BLOCK_DEFAULT = new String[] {"2"};
    
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
    
//    /**
//     * Road stripes
//     */
//    public int ROAD_STRIPES;
//    /**
//     * Default road stripes: 35
//     */
//    public static int ROAD_STRIPES_DEFAULT = 35;
//    
//    /**
//     * Road stripes data value (byte)
//     */
//    public int ROAD_STRIPES_DATA;
//    /**
//     * Default road stripes data value: (byte) 0
//     */
//    public static int ROAD_STRIPES_DATA_DEFAULT = 0;
//    
//    /**
//     * Wall block data value (byte)
//     */
//    public int WALL_BLOCK_DATA;
//    /**
//     * Default wall block data value: (byte) 0
//     */
//    public static int WALL_BLOCK_DATA_DEFAULT = 0;
//    
//    /**
//     * Wall filling data value (byte)
//     */
//    public int WALL_FILLING_DATA;
//    /**
//     * Default wall filling data value: (byte) 0
//     */
//    public static int WALL_FILLING_DATA_DEFAULT = 0;
    /**
     * Road block
     */
    public String ROAD_BLOCK;
    /**
     * Default road block: 155
     */
    public static String ROAD_BLOCK_DEFAULT = "155:0";
//    
//    /**
//     * Road block data value (byte)
//     */
//    public int ROAD_BLOCK_DATA;
//    /**
//     * Default road block data value: (byte) 0
//     */
//    public static int ROAD_BLOCK_DATA_DEFAULT = 0;
    
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


    public boolean SCHEMATIC_ON_CLAIM = false;
    public String SCHEMATIC_FILE = "null";
    
}
