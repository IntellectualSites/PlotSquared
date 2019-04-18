package com.plotsquared.sponge.generator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.ArrayList;
import java.util.List;

public class SpongePlotGenerator implements WorldGeneratorModifier, GeneratorWrapper<WorldGeneratorModifier> {
    
    private final IndependentPlotGenerator plotGenerator;
    private final List<GenerationPopulator> populators = new ArrayList<>();
    private final boolean loaded = false;
    private final WorldGeneratorModifier platformGenerator;
    private final boolean full;
    private PlotManager manager;
    
    public SpongePlotGenerator(IndependentPlotGenerator generator) {
        this.plotGenerator = generator;
        this.platformGenerator = this;
        this.full = true;
        MainUtil.initCache();
    }
    
    public SpongePlotGenerator(WorldGeneratorModifier wgm) {
        this.plotGenerator = null;
        this.platformGenerator = wgm;
        this.full = false;
        MainUtil.initCache();
    }

    @Override
    public String getId() {
        if (this.plotGenerator == null) {
            if (this.platformGenerator != this) {
                return this.platformGenerator.getId();
            }
            return "null";
        }
        return this.plotGenerator.getName();
    }
    
    @Override
    public String getName() {
        if (this.plotGenerator == null) {
            if (this.platformGenerator != this) {
                return this.platformGenerator.getName();
            }
            return "null";
        }
        return this.plotGenerator.getName();
    }
    
    @Override
    public void modifyWorldGenerator(WorldProperties world, DataContainer settings, WorldGenerator worldGenerator) {
        String worldName = world.getWorldName();
        worldGenerator.setBaseGenerationPopulator(new SpongeTerrainGen(this.plotGenerator));
        worldGenerator.setBiomeGenerator(buffer -> {
            PlotArea area = PS.get().getPlotArea(worldName, null);
            if (area != null) {
                BiomeType biome = SpongeUtil.getBiome(area.PLOT_BIOME);
                Vector3i min = buffer.getBiomeMin();
                Vector3i max = buffer.getBiomeMax();
                for (int x = min.getX(); x <= max.getX(); x++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        buffer.setBiome(x, 0, z, biome);
                }
            }
            }
    });
        for (BiomeType type : ReflectionUtils.<BiomeType> getStaticFields(BiomeTypes.class)) {
            BiomeGenerationSettings biomeSettings = worldGenerator.getBiomeSettings(type);
            biomeSettings.getGenerationPopulators().clear();
            biomeSettings.getPopulators().clear();
            biomeSettings.getGroundCoverLayers().clear();
    }
        worldGenerator.getGenerationPopulators().clear();
        worldGenerator.getPopulators().clear();
        PS.get().loadWorld(worldName, this);
    }
    
    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return this.plotGenerator;
    }
    
    @Override
    public WorldGeneratorModifier getPlatformGenerator() {
        return this.platformGenerator;
    }
    
    @Override
    public void augment(PlotArea area) {
        SpongeAugmentedGenerator.get(SpongeUtil.getWorld(area.worldname));
    }
    
    @Override
    public boolean isFull() {
        return this.full;
    }
    
}
