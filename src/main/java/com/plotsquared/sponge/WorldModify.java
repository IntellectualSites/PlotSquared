package com.plotsquared.sponge;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

public class WorldModify implements WorldGeneratorModifier {
    private SpongeMain main;

    public WorldModify(SpongeMain main) {
        this.main = main;
    }

    @Override
    public void modifyWorldGenerator(WorldCreationSettings world, DataContainer settings, WorldGenerator worldGenerator) {
        worldGenerator.setBaseGeneratorPopulator(new PlotGen(main, world.getWorldName(), world.getSeed()));
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
