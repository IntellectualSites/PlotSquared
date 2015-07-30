package com.plotsquared.sponge.generator;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GeneratorPopulator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;

public abstract class SpongePlotPopulator<T extends SpongePlotGenerator> implements GeneratorPopulator {

    public int X;
    public int Z;
    public String worldname;
    private World world;
    private PseudoRandom random = new PseudoRandom();
    private MutableBlockVolume buffer;
    public final T generator;
    
    public SpongePlotPopulator(T generator) {
        this.generator = generator;
    }
    
//    @Override
//    public void populate(Chunk chunk, Random random) {
//        this.world = chunk.getWorld();
//        this.worldname = world.getName();
//        Vector3i min = chunk.getBlockMin();

//    }
    
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomeBase) {
        try {
            this.world = world;
            this.worldname = world.getName();
            this.buffer = buffer;
            Vector3i min = buffer.getBlockMin();
            this.X = min.getX();
            this.Z = min.getZ();
            int cx = X >> 4;
            int cz = Z >> 4;
            int h = 1;
            final int prime = 13;
            h = (prime * h) + cx;
            h = (prime * h) + cz;
            this.random.state = h;
            
            // TODO plot clearing stuff
            
            populate(world, ChunkManager.CURRENT_PLOT_CLEAR, random, cx, cz);
        }
        catch (Exception e) {
            PS.debug("ERROR GENERATING CHUNK!");
            e.printStackTrace();
        }
    };
    
    public abstract void populate(World world, RegionWrapper requiredRegion, PseudoRandom random, int cx, int cz);
    
    /**
     * Set the id and data at a location. (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param id
     * @param data
     */
    public void setBlock(int x, int y, int z, BlockState state) {
        buffer.setBlock(X + x, y, Z + z, state);
    }
    
    public void setBlock(int x, int y, int z, BlockState[] states) {
        if (states.length == 1) {
            setBlock(x,y,z,states[0]);
        }
        setBlock(x,y,z,states[random.random(states.length)]);
    }
    
    /**
     * check if a region contains a location. (x, z) must be between [0,15], [0,15]
     * @param plot
     * @param x
     * @param z
     * @return
     */
    public boolean contains(final RegionWrapper plot, final int x, final int z) {
        int xx = X + x;
        int zz = Z + z;
        return ((xx >= plot.minX) && (xx <= plot.maxX) && (zz >= plot.minZ) && (zz <= plot.maxZ));
    }
    
}
