package com.plotsquared.sponge.listener;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;

public class WorldEvents {
    
    @Listener
    public void onLoadWorld(LoadWorldEvent event) {
        final World world = event.getTargetWorld();
        final String name = world.getName();
        WorldGenerator generator = world.getWorldGenerator();
        GenerationPopulator terrain = generator.getBaseGenerationPopulator();
        if (terrain instanceof GeneratorWrapper) {
            GeneratorWrapper stg = (GeneratorWrapper) terrain;
            PS.get().loadWorld(name, stg);
        }
        else {
            PS.get().loadWorld(name, null);
        }
    }
}
