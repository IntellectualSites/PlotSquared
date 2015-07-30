package com.plotsquared.sponge.generator;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;

public abstract class SpongePlotGenerator implements WorldGenerator {

    public final String world;
    
    public SpongePlotGenerator(String world) {
        this.world = world;
    }

    @Override
    public GeneratorPopulator getBaseGeneratorPopulator() {
        return getGenerator();
    }

    @Override
    public BiomeGenerator getBiomeGenerator() {
        return getPlotBiomeProvider();
    }

    @Override
    public List<GeneratorPopulator> getGeneratorPopulators() {
        List<GeneratorPopulator> pops = new ArrayList<>();
        pops.addAll(this.getPlotPopulators());
        return pops;
    }

    @Override
    public List<Populator> getPopulators() {
        return new ArrayList<>();
    }

    @Override
    public void setBaseGeneratorPopulator(GeneratorPopulator arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomeGenerator) {
        // TODO
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    public abstract SpongePlotPopulator getGenerator();
    
    public abstract BiomeGenerator getPlotBiomeProvider();
    
    public abstract List<SpongePlotPopulator> getPlotPopulators();
    
    /**
     * This is called when the generator is initialized. 
     * You don't need to do anything with it necessarily.
     * @param plotworld
     */
    public abstract void init(PlotWorld plotworld);
    
    /**
     * Return a new instance of the PlotWorld for a world 
     * @param world
     * @return
     */
    public abstract PlotWorld getNewPlotWorld(final String world);

    /**
     * Get the PlotManager class for this generator
     * @return
     */
    public abstract PlotManager getPlotManager();
    
    /**
     * If you need to do anything fancy for /plot setup<br>
     *  - Otherwise it will just use the PlotWorld configuration<br>
     * Feel free to extend BukkitSetupUtils and customize world creation
     * @param object
     */
    public void processSetup(SetupObject object) {}
}
