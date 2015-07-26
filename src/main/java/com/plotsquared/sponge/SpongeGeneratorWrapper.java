package com.plotsquared.sponge;

import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;

public class SpongeGeneratorWrapper extends PlotGenerator<WorldGenerator>{

    public SpongeGeneratorWrapper(String world, WorldGenerator generator) {
        super(world, generator);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void initialize(PlotWorld plotworld) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void augment(PlotCluster cluster, PlotWorld plotworld) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setGenerator(String generator) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PlotWorld getNewPlotWorld(String world) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlotManager getPlotManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFull() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
