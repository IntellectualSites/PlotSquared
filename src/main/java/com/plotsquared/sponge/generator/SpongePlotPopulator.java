package com.plotsquared.sponge.generator;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;

public abstract class SpongePlotPopulator<T extends SpongePlotGenerator> implements GenerationPopulator {
    
    public int X;
    public int Z;
    public String worldname;
    private final PseudoRandom random = new PseudoRandom();
    private MutableBlockVolume buffer;
    public final T generator;
    
    public SpongePlotPopulator(final T generator) {
        this.generator = generator;
    }
    
    //    @Override
    //    public void populate(Chunk chunk, Random random) {
    //        this.world = chunk.getWorld();
    //        this.worldname = world.getName();
    //        Vector3i min = chunk.getBlockMin();
    
    //    }
    
    @Override
    public void populate(final World world, final MutableBlockVolume buffer, final ImmutableBiomeArea biomeBase) {
        try {
            this.worldname = world.getName();
            this.buffer = buffer;
            final Vector3i min = buffer.getBlockMin();
            this.X = min.getX();
            this.Z = min.getZ();
            final int cx = X >> 4;
            final int cz = Z >> 4;
            int h = 1;
            final int prime = 13;
            h = (prime * h) + cx;
            h = (prime * h) + cz;
            this.random.state = h;
            
            // TODO plot clearing stuff
            
            populate(world, ChunkManager.CURRENT_PLOT_CLEAR, random, cx, cz);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void populate(final World world, final RegionWrapper requiredRegion, final PseudoRandom random, final int cx, final int cz);
    
    /**
     * Set the id and data at a location. (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param state
     */
    public void setBlock(final int x, final int y, final int z, final BlockState state) {
        buffer.setBlock(X + x, y, Z + z, state);
    }
    
    public void setBlock(final int x, final int y, final int z, final BlockState[] states) {
        if (states.length == 1) {
            setBlock(x, y, z, states[0]);
        }
        setBlock(x, y, z, states[random.random(states.length)]);
    }
    
    /**
     * check if a region contains a location. (x, z) must be between [0,15], [0,15]
     * @param plot
     * @param x
     * @param z
     * @return
     */
    public boolean contains(final RegionWrapper plot, final int x, final int z) {
        final int xx = X + x;
        final int zz = Z + z;
        return ((xx >= plot.minX) && (xx <= plot.maxX) && (zz >= plot.minZ) && (zz <= plot.maxZ));
    }
    
}
