package com.plotsquared.sponge.generator;

import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.SetupObject;

public class SpongeGeneratorWrapper extends PlotGenerator<WorldGenerator>{

    public final boolean full;
    
    public SpongeGeneratorWrapper(String world, WorldGenerator generator) {
        super(world, generator);
        full = (generator instanceof SpongePlotGenerator);
    }

    @Override
    public void initialize(PlotWorld plotworld) {
        if (generator instanceof SpongePlotGenerator) {
            ((SpongePlotGenerator) generator).init(plotworld);
        }
    }

    @Override
    public void augment(PlotCluster cluster, PlotWorld plotworld) {
        if (generator instanceof SpongePlotGenerator) {
            SpongePlotGenerator plotgen = (SpongePlotGenerator) generator;
            if (cluster != null) {
                // TODO Augment partial
                throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
            }
            else {
                // TODO augment full
                throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
            }
        }
    }

    @Override
    public void setGenerator(String gen_string) {
        if (gen_string == null) {
            generator = new SpongeBasicGen(world);
        } else {
            PlotGenerator<WorldGenerator> gen_wrapper = (PlotGenerator<WorldGenerator>) PS.get().IMP.getGenerator(world, gen_string);
            if (gen_wrapper != null) {
                generator = gen_wrapper.generator;
            }
        }
    }

    @Override
    public PlotWorld getNewPlotWorld(String world) {
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
    public void processSetup(SetupObject object) {
        if (generator instanceof SpongePlotGenerator) {
            ((SpongePlotGenerator) generator).processSetup(object);
        }
    }
    
}
