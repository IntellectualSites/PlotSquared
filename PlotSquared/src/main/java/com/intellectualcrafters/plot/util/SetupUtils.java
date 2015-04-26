package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;

public abstract class SetupUtils {

    public static SetupUtils manager;
    
    public final static Map<String, SetupObject> setupMap = new HashMap<>();
    public static HashMap<String, ChunkGenerator> generators = new HashMap<>();

    public abstract void updateGenerators();

    public abstract String getGenerator(PlotWorld plotworld);
    
    public abstract String setupWorld(final SetupObject object);
}
