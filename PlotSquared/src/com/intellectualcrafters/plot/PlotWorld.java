package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
public abstract class PlotWorld {
    
    public boolean AUTO_MERGE;
    public static boolean AUTO_MERGE_DEFAULT = false;
    
    public boolean MOB_SPAWNING;
    public static boolean MOB_SPAWNING_DEFAULT = false;
    
    public Biome PLOT_BIOME;
    public static Biome PLOT_BIOME_DEFAULT = Biome.FOREST;
    
    public boolean PLOT_CHAT;
    public static boolean PLOT_CHAT_DEFAULT = false;
    
    public boolean SCHEMATIC_CLAIM_SPECIFY = false;
    public static boolean SCHEMATIC_CLAIM_SPECIFY_DEFAULT = false;
    
    public boolean SCHEMATIC_ON_CLAIM;
    public static boolean SCHEMATIC_ON_CLAIM_DEFAULT = false;
    
    public String SCHEMATIC_FILE;
    public static String SCHEMATIC_FILE_DEFAULT = "null";
    
    public List<String> SCHEMATICS;
    public static List<String> SCHEMATICS_DEFAULT = null;
    
    public List<String> DEFAULT_FLAGS;
    public static List<String> DEFAULT_FLAGS_DEFAULT = new ArrayList<String>();
    
    public boolean USE_ECONOMY;
    public static boolean USE_ECONOMY_DEFAULT = false;
  
    public double PLOT_PRICE;
    public static double PLOT_PRICE_DEFAULT = 100;
    
    public double MERGE_PRICE;
    public static double MERGE_PRICE_DEFAULT = 100;

    public PlotWorld(String worldname) {
        this.worldname = worldname;
    }
    
    /**
     * When a world is created, the following method will be called for each node set in the configuration
     *  - You may ignore this if you generator does not support configuration, or if you want to implement your own methods
     * 
     * @param key
     * @param value
     */
    public void loadConfiguration(ConfigurationSection config) {
        this.MOB_SPAWNING = config.getBoolean("natural_mob_spawning");
        this.AUTO_MERGE = config.getBoolean("plot.auto_merge");
        this.PLOT_BIOME = (Biome) Configuration.BIOME.parseString(config.getString("plot.biome"));
        this.SCHEMATIC_ON_CLAIM = config.getBoolean("schematic.on_claim");
        this.SCHEMATIC_FILE = config.getString("schematic.file");
        this.SCHEMATIC_CLAIM_SPECIFY = config.getBoolean("schematic.specify_on_claim");
        this.SCHEMATICS = config.getStringList("schematic.schematics");
        this.USE_ECONOMY = config.getBoolean("economy.use");
        this.PLOT_PRICE = config.getDouble("economy.prices.claim");
        this.MERGE_PRICE = config.getDouble("economy.prices.merge");
        this.PLOT_CHAT = config.getBoolean("chat.enabled");
        this.DEFAULT_FLAGS = config.getStringList("flags.default");
    }
    
    public void saveConfiguration(ConfigurationSection config) {
        
        /*
         * Saving core plotworld settings
         */
        config.set("natural_mob_spawning",this.MOB_SPAWNING);
        config.set("plot.auto_merge",this.AUTO_MERGE);
        config.set("plot.biome",this.PLOT_BIOME.name());
        config.set("schematic.on_claim",this.SCHEMATIC_ON_CLAIM);
        config.set("schematic.file",this.SCHEMATIC_FILE);
        config.set("schematic.specify_on_claim",this.SCHEMATIC_CLAIM_SPECIFY);
        config.set("schematic.schematics",this.SCHEMATICS);
        config.set("economy.use",this.USE_ECONOMY);
        config.set("economy.prices.claim",this.PLOT_PRICE);
        config.set("economy.prices.merge",this.MERGE_PRICE);
        config.set("chat.enabled",this.PLOT_CHAT);
        config.set("flags.default",this.DEFAULT_FLAGS);
        
        ConfigurationNode[] settings = getSettingNodes();
        
        /*
         * Saving generator specific settings
         */
        for (ConfigurationNode setting:settings) {
            config.set(setting.getConstant(), setting.getValue());
        }
    }
    
    public String worldname;

    /**
     * Used for the <b>/plot setup</b> command
     * Return null if you do not want to support this feature
     * 
     * @return ConfigurationNode[]
     */
    public abstract ConfigurationNode[] getSettingNodes();
}