package com.plotsquared.sponge.listener;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;

import com.intellectualcrafters.plot.PS;
import com.plotsquared.sponge.generator.SpongeTerrainGen;

public class WorldEvents {
    
    @Listener
    public void onLoadWorld(LoadWorldEvent event) {
        final World world = event.getTargetWorld();
        final String name = world.getName();
        WorldGenerator generator = world.getWorldGenerator();
        GenerationPopulator terrain = generator.getBaseGenerationPopulator();
        if (terrain instanceof SpongeTerrainGen) {
            SpongeTerrainGen stg = (SpongeTerrainGen) terrain;
            PS.get().loadWorld(name, stg.parent);
        }
        else if (PS.get().config.contains("worlds." + name)) {
            PS.get().loadWorld(name, null);
        }
    }
}
