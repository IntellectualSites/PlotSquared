package com.plotsquared.sponge.generator;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;
import com.plotsquared.sponge.util.block.GenChunk;

public class SpongePlotGenerator implements WorldGeneratorModifier, GeneratorWrapper<WorldGeneratorModifier> {
    
    private final PseudoRandom random = new PseudoRandom();
    private final IndependentPlotGenerator plotGenerator;
    private final List<GenerationPopulator> populators = new ArrayList<>();
    private final boolean loaded = false;
    private PlotManager manager;
    private final WorldGeneratorModifier platformGenerator;
    private final boolean full;
    
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
        return "PlotSquared";
    }
    
    @Override
    public String getName() {
        return "PlotSquared";
    }
    
    @Override
    public void modifyWorldGenerator(WorldCreationSettings settings, DataContainer data, WorldGenerator wg) {
        if (full) {
            final String worldname = settings.getWorldName();
            wg.getGenerationPopulators().clear();
            wg.getPopulators().clear();
            wg.setBaseGenerationPopulator(new GenerationPopulator() {
                @Override
                public void populate(World world, MutableBlockVolume terrain, ImmutableBiomeArea biome) {
                    System.out.println("POPULATE!");
                    Vector3i size = terrain.getBlockSize();
                    if (size.getX() != 16 || size.getZ() != 16) {
                        throw new UnsupportedOperationException("NON CHUNK POPULATION NOT SUPPORTED");
                    }
                    String worldname = world.getName();
                    Vector3i min = terrain.getBlockMin();
                    int cx = min.getX() >> 4;
                    int cz = min.getZ() >> 4;
                    ChunkWrapper wrap = SetQueue.IMP.new ChunkWrapper(worldname, cx, cz);
                    // Create the result object
                    GenChunk result = new GenChunk(terrain, null, wrap);
                    // Catch any exceptions
                    try {
                        // Fill the result data if necessary
                        if (platformGenerator != SpongePlotGenerator.this) {
                            throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
                        } else {
                            // Set random seed
                            random.state = (cx << 16) | (cz & 0xFFFF);
                            // Process the chunk
                            result.modified = false;
                            ChunkManager.preProcessChunk(result);
                            if (result.modified) {
                                return;
                            }
                            PlotArea area = PS.get().getPlotArea(world.getName(), null);
                            plotGenerator.generateChunk(result, area, random);
                            ChunkManager.postProcessChunk(result);
                            return;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            wg.setBiomeGenerator(new BiomeGenerator() {
                @Override
                public void generateBiomes(MutableBiomeArea buffer) {
                    PlotArea area = PS.get().getPlotArea(worldname, null);
                    if (area != null) {
                        BiomeType biome = SpongeUtil.getBiome(area.PLOT_BIOME);
                        Vector2i min = buffer.getBiomeMin();
                        Vector2i max = buffer.getBiomeMax();
                        for (int x = min.getX(); x <= max.getX(); x++) {
                            for (int z = min.getY(); z <= max.getY(); z++) {
                                buffer.setBiome(x, z, biome);
                            }
                        }
                    }
                }
            });
        } else {
            throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        }
    }
    
    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return plotGenerator;
    }
    
    @Override
    public WorldGeneratorModifier getPlatformGenerator() {
        return platformGenerator;
    }
    
    @Override
    public void augment(PlotArea area) {
        SpongeAugmentedGenerator.get(SpongeUtil.getWorld(area.worldname));
    }
    
    @Override
    public boolean isFull() {
        return full;
    }
    
}
