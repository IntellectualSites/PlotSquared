package com.plotsquared.sponge.generator;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

public class WorldModify implements WorldGeneratorModifier {

    private SpongePlotGenerator plotgen;

    public WorldModify(SpongePlotGenerator plotgen) {
        this.plotgen = plotgen;
    }

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator gen) {
        gen.setBaseGeneratorPopulator(plotgen.getBaseGeneratorPopulator());
        gen.setBiomeGenerator(plotgen.getBiomeGenerator());
//        if (gen instanceof SpongeWorldGenerator) {
//            SpongePlotGenerator plotgen = (SpongePlotGenerator) gen;
//            plotgen.setBaseGeneratorPopulator(plotgen.getGenerator());
//            plotgen.setBiomeGenerator(plotgen.getPlotBiomeProvider());
//        }
    }
    
    @Override
    public String getName() {
        return "PlotSquared";
    }

    @Override
    public String getId() {
        return "PlotSquared";
    }
}
