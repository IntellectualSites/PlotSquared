package com.plotsquared.sponge.generator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.plotsquared.sponge.util.block.GenChunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

public class SpongeTerrainGen implements GenerationPopulator {
    
    public final SpongePlotGenerator parent;
    public final IndependentPlotGenerator child;
    private final PseudoRandom random = new PseudoRandom();
    
    public SpongeTerrainGen(SpongePlotGenerator parent, IndependentPlotGenerator ipg) {
        this.parent = parent;
        this.child = ipg;
    }
    
    @Override
    public void populate(World world, MutableBlockVolume terrain, ImmutableBiomeArea biomes) {
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
}
