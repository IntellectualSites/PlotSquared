package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.SetupObject;

public abstract class PlotGenerator<T> {

    public final T generator;

    public PlotGenerator(T generator) {
        this.generator = generator;
    }

    public abstract void initialize(PlotArea plotArea);

    /**
     * TYPE = 2;
     * new AugmentedPopulator(world, generator, cluster, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
     * TYPE = 1
     * new AugmentedPopulator(world, gen_class, null, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
     *
     * @param area
     */
    public abstract void augment(PlotArea area);

    public abstract PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max);

    public abstract PlotManager getPlotManager();

    public abstract boolean isFull();

    public abstract String getName();

    public abstract void processSetup(SetupObject object);
}
