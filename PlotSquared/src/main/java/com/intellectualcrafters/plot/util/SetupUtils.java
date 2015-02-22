package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.Map;

import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.bukkit.BukkitSetupUtils;

public abstract class SetupUtils {
    
    public static SetupUtils manager = new BukkitSetupUtils();
    
    public final static Map<String, SetupObject> setupMap = new HashMap<>();
    public static HashMap<String, PlotGenerator> generators = new HashMap<>();
    
    public abstract void updateGenerators();
    
    public abstract String setupWorld(final SetupObject object);
}
