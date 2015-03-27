package com.intellectualcrafters.plot.object;

import java.util.HashMap;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public abstract class PlotPopulator extends BlockPopulator{
    
    private int X;
    private int Z;
    private World world;
    
    @Override
    public void populate(World world, Random rand, Chunk chunk) {
        this.world = world;
        this.X = chunk.getX() << 4;
        this.Z = chunk.getZ() << 4;
        
        if (ChunkManager.FORCE_PASTE) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    final PlotLoc loc = new PlotLoc((short) (X + x), (short) (Z + z));
                    final HashMap<Short, Byte> blocks = ChunkManager.GENERATE_DATA.get(loc);
                    for (final Entry<Short, Byte> entry : blocks.entrySet()) {
                        setBlock(x, entry.getKey(), z, entry.getValue());
                    }
                }
            }
            return;
        }
        
    }
    
    public abstract void populate(World world, RegionWrapper requiredRegion, PseudoRandom random, int cx, int cz);
    
    /**
     * Set the id and data at a location (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param id
     * @param data
     */
    public void setBlock(int x, int y, int z, short id, byte data) {
        BukkitUtil.setBlock(world, X + x, y, Z + z, id, data);
    }
    
    /**
     * Set the data at a location (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param data
     */
    public void setBlock(int x, int y, int z, byte data) {
        world.getBlockAt(X + x, y, Z + z).setTypeId(data);
    }
    
    public boolean contains(final RegionWrapper plot, final int x, final int z) {
        int xx = X + x;
        int zz = Z + z;
        return ((xx >= plot.minX) && (xx <= plot.maxX) && (zz >= plot.minZ) && (zz <= plot.maxZ));
    }
    
}
