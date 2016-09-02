package com.plotsquared.sponge.generator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.sponge.util.SpongeUtil;
import com.plotsquared.sponge.util.block.GenChunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

public class SpongeTerrainGen implements GenerationPopulator, GeneratorWrapper<GenerationPopulator> {
    
    public final IndependentPlotGenerator child;
    private final boolean full;
    private final GenerationPopulator platformGenerator;
    private final PseudoRandom random = new PseudoRandom();
    
    public SpongeTerrainGen(IndependentPlotGenerator ipg) {
        this.child = ipg;
        this.full = true;
        this.platformGenerator = this;
        MainUtil.initCache();
    }

    public SpongeTerrainGen(GenerationPopulator populator) {
        this.child = null;
        this.platformGenerator = populator;
        this.full = false;
        MainUtil.initCache();
    }
    
    @Override
    public void populate(World world, MutableBlockVolume terrain, ImmutableBiomeArea biomes) {
        if (platformGenerator != this) {
            platformGenerator.populate(world, terrain, biomes);
            return;
        }
        Vector3i size = terrain.getBlockSize();
        if (size.getX() != 16 || size.getZ() != 16) {
            throw new UnsupportedOperationException("NON CHUNK POPULATION NOT SUPPORTED");
        }
        String worldname = world.getName();
        Vector3i min = terrain.getBlockMin();
        int cx = min.getX() >> 4;
        int cz = min.getZ() >> 4;
        ChunkWrapper wrap = new ChunkWrapper(worldname, cx, cz);
        // Create the result object
        GenChunk result = new GenChunk(terrain, null, wrap);
        // Catch any exceptions
        try {
            // Set random seed
            random.state = (cx << 16) | (cz & 0xFFFF);
            // Process the chunk
            result.modified = false;
            ChunkManager.preProcessChunk(result);
            if (result.modified) {
                return;
            }
            // Fill the result data
            PlotArea area = PS.get().getPlotArea(world.getName(), null);
            child.generateChunk(result, area, random);
            child.populateChunk(result, area, random);
            ChunkManager.postProcessChunk(result);
            return;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public IndependentPlotGenerator getPlotGenerator() {
        return child;
    }

    @Override
    public GenerationPopulator getPlatformGenerator() {
        return platformGenerator;
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
