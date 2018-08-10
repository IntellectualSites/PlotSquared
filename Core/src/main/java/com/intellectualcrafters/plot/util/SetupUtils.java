package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.SetupObject;

import java.util.HashMap;

public abstract class SetupUtils {

    public static SetupUtils manager;

    public static HashMap<String, GeneratorWrapper<?>> generators = new HashMap<>();

    public abstract void updateGenerators();

    public abstract String getGenerator(final PlotArea plotArea);

    public abstract String setupWorld(final SetupObject object);

    public abstract void unload(String world, boolean save);
}
