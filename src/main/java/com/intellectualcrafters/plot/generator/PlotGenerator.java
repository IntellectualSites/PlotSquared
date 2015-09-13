package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;

public abstract class PlotGenerator<T> {
    public final String world;
    public T generator;
    
    public PlotGenerator(final String world, final T generator) {
        this.world = world;
        this.generator = generator;
    }
    
    public abstract void initialize(final PlotWorld plotworld);
    
    /**
     * TYPE = 2;
     * new AugmentedPopulator(world, generator, cluster, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
     * TYPE = 1
     * new AugmentedPopulator(world, gen_class, null, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
     * @param generator
     * @param cluster Will be the cluster, or null
     * @param plotworld
     */
    public abstract void augment(final PlotCluster cluster, final PlotWorld plotworld);
    
    /**
     *
                    if (gen_string == null) {
                        generator = new HybridGen(world);
                    } else {
                        generator = (PlotGenerator) IMP.getGenerator(world, gen_string);
                    }

     * @param generator
     */
    public abstract void setGenerator(final String generator);
    
    public abstract PlotWorld getNewPlotWorld(final String world);
    
    public abstract PlotManager getPlotManager();
    
    public abstract boolean isFull();
    
    public abstract String getName();
    
    public abstract void processSetup(final SetupObject object);
}
