package com.plotsquared.sponge.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.SetupObject;

public abstract class SpongePlotGenerator implements WorldGenerator {
    
    public String world;
    
    public SpongePlotGenerator(final String world) {
        this.world = world;
    }
    
    @Override
    public GenerationPopulator getBaseGenerationPopulator() {
        return getGenerator();
    }
    
    @Override
    public BiomeGenerator getBiomeGenerator() {
        return getPlotBiomeProvider();
    }
    
    @Override
    public List<Populator> getPopulators() {
        return new ArrayList<>();
    }
    
    @Override
    public void setBiomeGenerator(final BiomeGenerator biomeGenerator) {
        // TODO
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public BiomeGenerationSettings getBiomeSettings(BiomeType type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public List<GenerationPopulator> getGenerationPopulators() {
        final List<GenerationPopulator> pops = new ArrayList<>();
        pops.addAll(getPlotPopulators());
        return pops;
    }
    
    @Override
    public List<GenerationPopulator> getGenerationPopulators(Class<? extends GenerationPopulator> clazz) {
        List<GenerationPopulator> list = getGenerationPopulators();
        Iterator<GenerationPopulator> iter = list.iterator();
        while (iter.hasNext()) {
            GenerationPopulator pop = iter.next();
            if (!clazz.isInstance(pop)) {
                iter.remove();
            }
        }
        return list;
    }
    
    @Override
    public List<Populator> getPopulators(Class<? extends Populator> arg0) {
        return new ArrayList<>();
    }
    
    @Override
    public void setBaseGenerationPopulator(GenerationPopulator arg0) {
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
    public abstract void init(final PlotArea plotworld);
    
    /**
     * Return a new instance of the PlotArea for a world
     * @param world
     * @return
     */
    public abstract PlotArea getNewPlotWorld(final String world);
    
    /**
     * Get the PlotManager class for this generator
     * @return
     */
    public abstract PlotManager getPlotManager();
    
    /**
     * If you need to do anything fancy for /plot setup<br>
     *  - Otherwise it will just use the PlotArea configuration<br>
     * Feel free to extend BukkitSetupUtils and customize world creation
     * @param object
     */
    public void processSetup(final SetupObject object) {}
}
