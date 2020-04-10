package com.github.intellectualsites.plotsquared.util;

import com.github.intellectualsites.plotsquared.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;

import java.util.HashMap;

public abstract class SetupUtils {

    public static SetupUtils manager;

    public static HashMap<String, GeneratorWrapper<?>> generators = new HashMap<>();

    public abstract void updateGenerators();

    public abstract String getGenerator(final PlotArea plotArea);

    public abstract String setupWorld(final SetupObject object);

    public abstract void unload(String world, boolean save);
}
