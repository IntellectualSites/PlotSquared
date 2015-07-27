package com.plotsquared.bukkit.generator;

import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;

public class BukkitGeneratorWrapper extends PlotGenerator<ChunkGenerator> {

    public final boolean full;
    
    public BukkitGeneratorWrapper(String world, ChunkGenerator generator) {
        super(world, generator);
        full = generator != null;
    }

    @Override
    public void initialize(PlotWorld plotworld) {
        if (generator instanceof BukkitPlotGenerator) {
            ((BukkitPlotGenerator) generator).init(plotworld);
        }
    }

    @Override
    public void augment(PlotCluster cluster, PlotWorld plotworld) {
        if (generator instanceof BukkitPlotGenerator) {
            BukkitPlotGenerator plotgen = (BukkitPlotGenerator) generator;
            if (cluster != null) {
                new AugmentedPopulator(world, plotgen, cluster, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
            }
            else {
                new AugmentedPopulator(world, plotgen, null, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
            }
        }
    }

    @Override
    public void setGenerator(String gen_string) {
        if (gen_string == null) {
            generator = new HybridGen(world);
        } else {
            PlotGenerator<ChunkGenerator> gen_wrapper = (PlotGenerator<ChunkGenerator>) PS.get().IMP.getGenerator(world, gen_string);
            if (gen_wrapper != null) {
                generator = gen_wrapper.generator;
            }
        }
    }

    @Override
    public PlotWorld getNewPlotWorld(String world) {
        if (!(generator instanceof BukkitPlotGenerator)) {
            return null;
        }
        return ((BukkitPlotGenerator) generator).getNewPlotWorld(world);
    }

    @Override
    public PlotManager getPlotManager() {
        if (!(generator instanceof BukkitPlotGenerator)) {
            return null;
        }
        return ((BukkitPlotGenerator) generator).getPlotManager();
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
    
}
