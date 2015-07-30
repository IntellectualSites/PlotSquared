package com.plotsquared.sponge.generator;

import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.gen.BiomeGenerator;

import com.flowpowered.math.vector.Vector2i;
import com.intellectualcrafters.plot.object.PlotWorld;

public class SpongeBasicBiomeProvider implements BiomeGenerator {

    private PlotWorld plotworld;

    public SpongeBasicBiomeProvider(PlotWorld plotworld) {
        this.plotworld = plotworld;
    }
    
    @Override
    public void generateBiomes(MutableBiomeArea biomeBase) {
        Vector2i min = biomeBase.getBiomeMin();
        int bx = min.getX();
        int bz = min.getY();
        BiomeType biome = BiomeTypes.FOREST;
        try {
            biome = (BiomeType) BiomeTypes.class.getField(plotworld.PLOT_BIOME.toUpperCase()).get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int x = bx; x < bx + 16; x++) {
            for (int z = bz; z < bz + 16; z++) {
                biomeBase.setBiome(x, z, biome);
            }
        }
    }
    
}
