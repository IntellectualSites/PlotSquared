package com.plotsquared.core.util;

import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.SetupObject;

import java.util.HashMap;

public abstract class SetupUtils {

    public static SetupUtils manager;

    public static HashMap<String, GeneratorWrapper<?>> generators = new HashMap<>();

    public abstract void updateGenerators();

    public abstract String getGenerator(final PlotArea plotArea);

    public abstract String setupWorld(final SetupObject object);

    public abstract void unload(String world, boolean save);
}
