package com.plotsquared.sponge.generator;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ClusterManager;

public class WorldModify implements WorldGeneratorModifier {
    
    private final SpongePlotGenerator plotgen;
    private final boolean augment;
    
    public WorldModify(final SpongePlotGenerator plotgen, final boolean augment) {
        this.plotgen = plotgen;
        this.augment = augment;
    }
    
    @Override
    public void modifyWorldGenerator(final WorldCreationSettings world, final DataContainer settings, final WorldGenerator gen) {
        if (augment) {
            final String worldname = world.getWorldName();
            plotgen.world = worldname;
            final PlotWorld plotworld = plotgen.getNewPlotWorld(worldname);
            if (plotworld.TYPE == 2) {
                for (final PlotCluster cluster : ClusterManager.getClusters(worldname)) {
                    new AugmentedPopulator(worldname, gen, plotgen, cluster, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
                }
            } else {
                new AugmentedPopulator(worldname, gen, plotgen, null, plotworld.TERRAIN == 2, plotworld.TERRAIN != 2);
            }
        } else {
            gen.getGenerationPopulators().clear();
            gen.getPopulators().clear();
            gen.setBaseGenerationPopulator(plotgen.getBaseGenerationPopulator());
            gen.setBiomeGenerator(plotgen.getBiomeGenerator());
        }
    }
    
    @Override
    public String getName() {
        return "plotsquared";
    }
    
    @Override
    public String getId() {
        return "plotsquared";
    }
}
