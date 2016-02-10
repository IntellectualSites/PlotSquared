package com.plotsquared.sponge.generator;

import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.SetupObject;
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongeGeneratorWrapper extends PlotGenerator<WorldGenerator> {
    
    public final boolean full;
    
    public SpongeGeneratorWrapper(final String world, final WorldGenerator generator) {
        super(world, generator);
        full = (generator instanceof SpongePlotGenerator);
    }
    
    @Override
    public void initialize(final PlotArea plotworld) {
        if (generator instanceof SpongePlotGenerator) {
            ((SpongePlotGenerator) generator).init(plotworld);
        }
    }
    
    @Override
    public void augment(final PlotCluster cluster, final PlotArea plotworld) {
        if (generator instanceof SpongePlotGenerator) {
            final SpongePlotGenerator plotgen = (SpongePlotGenerator) generator;
            final World worldObj = SpongeUtil.getWorld(world);
            if (worldObj != null) {
                if (cluster != null) {
                    new AugmentedPopulator(world, worldObj.getWorldGenerator(), plotgen, cluster, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
                } else {
                    new AugmentedPopulator(world, worldObj.getWorldGenerator(), plotgen, null, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
                }
            }
        }
    }
    
    @Override
    public void setGenerator(final String gen_string) {
        if (gen_string == null) {
            generator = new SpongeBasicGen(world);
        } else {
            final PlotGenerator<WorldGenerator> gen_wrapper = (PlotGenerator<WorldGenerator>) PS.get().IMP.getGenerator(world, gen_string);
            if (gen_wrapper != null) {
                generator = gen_wrapper.generator;
            }
        }
    }
    
    @Override
    public PlotArea getNewPlotWorld(final String world) {
        if (!(generator instanceof SpongePlotGenerator)) {
            return null;
        }
        return ((SpongePlotGenerator) generator).getNewPlotWorld(world);
    }
    
    @Override
    public PlotManager getPlotManager() {
        if (!(generator instanceof SpongePlotGenerator)) {
            return null;
        }
        return ((SpongePlotGenerator) generator).getPlotManager();
    }
    
    @Override
    public boolean isFull() {
        return full;
    }
    
    @Override
    public String getName() {
        if (generator == null) {
            return "Null";
        }
        return generator.getClass().getName();
    }
    
    @Override
    public void processSetup(final SetupObject object) {
        if (generator instanceof SpongePlotGenerator) {
            ((SpongePlotGenerator) generator).processSetup(object);
        }
    }
    
}
