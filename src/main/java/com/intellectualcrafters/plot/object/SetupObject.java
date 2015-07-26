package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.plotsquared.bukkit.util.SetupUtils;

public class SetupObject {
    
    /**
     * Specify a SetupUtils object here to override the existing
     */
    public SetupUtils setupManager;
    
    /**
     * The current state
     */
    public int current = 0;
    
    /**
     * The index in generator specific settings
     */
    public int setup_index = 0;
    
    /**
     * The name of the world
     */
    public String world = null;
    
    /**
     * The name of the plot manager
     */
    public String plotManager = null;
    
    /**
     * The name of the generator to use for world creation
     */
    public String setupGenerator = null;
    
    /**
     * The management type
     */
    public int type;
    
    /**
     * The terrain type
     */
    public int terrain;
    
    /**
     * Generator specific configuration steps
     */
    public ConfigurationNode[] step = null;
}
