package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.Map;

import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;

public abstract class SetupUtils {
    
    public static SetupUtils manager;
    
    public final static Map<String, SetupObject> setupMap = new HashMap<>();
    public static HashMap<String, PlotGenerator<?>> generators = new HashMap<>();
    
    public abstract void updateGenerators();
    
    public abstract String getGenerator(final PlotWorld plotworld);
    
    public abstract String setupWorld(final SetupObject object);
    
    public abstract void removePopulator(final String world, final PlotCluster cluster);
}
