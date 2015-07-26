package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;

public abstract class PlotGenerator2 {
    public final String world;

    public PlotGenerator2(String world) {
        this.world = world;
    }
    
    public abstract void initialize(PlotWorld plotworld);
    
    /**
     * TYPE = 2;
     * new AugmentedPopulator(world, generator, cluster, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
     * TYPE = 1
     * new AugmentedPopulator(world, gen_class, null, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
     * @param generator
     * @param cluster Will be the cluster, or null
     * @param plotworld
     */
    public abstract void augment(String generator, PlotCluster cluster, PlotWorld plotworld);
    
    
    /**
     *
                    if (gen_string == null) {
                        generator = new HybridGen(world);
                    } else {
                        generator = (PlotGenerator) IMP.getGenerator(world, gen_string);
                    }
     
     * @param generator
     */
    public abstract void setGenerator(String generator);
    
    public abstract PlotWorld getNewPlotWorld(String world);
    
    public abstract PlotManager getPlotManager();
    
    public abstract boolean isFull();
    
    public abstract String getName();
}
