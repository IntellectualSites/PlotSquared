package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;
import org.bukkit.generator.ChunkGenerator;

import java.util.HashMap;
import java.util.Map;

public abstract class SetupUtils {

    public static SetupUtils manager;
    
    public final static Map<String, SetupObject> setupMap = new HashMap<>();
    public static HashMap<String, ChunkGenerator> generators = new HashMap<>();

    public abstract void updateGenerators();

    public abstract String getGenerator(PlotWorld plotworld);
    
    public abstract String setupWorld(final SetupObject object);
}
