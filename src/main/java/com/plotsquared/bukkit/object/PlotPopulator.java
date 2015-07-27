package com.plotsquared.bukkit.object;

import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public abstract class PlotPopulator extends BlockPopulator {
    
    private PseudoRandom random = new PseudoRandom();
    
    public int X;
    public int Z;
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
        populate(world, ChunkManager.CURRENT_PLOT_CLEAR, random, X, Z);
        if (ChunkManager.CURRENT_PLOT_CLEAR != null) {
            PlotLoc loc;
            for (Entry<PlotLoc, HashMap<Short, Byte>> entry : ChunkManager.GENERATE_DATA.entrySet()) {
                for (Entry<Short, Byte> entry2 : entry.getValue().entrySet()) {
                    loc = entry.getKey();
                    int xx = loc.x - X;
                    int zz = loc.z - Z;
                    if (xx >= 0 && xx < 16) {
                    	if (zz >= 0 && zz < 16) {
                    		setBlock(xx, entry2.getKey(), zz, entry2.getValue());
                    	}
                    }
                }
            }
        }
    }
    
    public abstract void populate(World world, RegionWrapper requiredRegion, PseudoRandom random, int cx, int cz);
    
    /**
     * Set the id and data at a location. (x, y, z) must be between [0,15], [0,255], [0,15]
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
     * Set the data at a location. (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param data
     */
    public void setBlock(int x, int y, int z, byte data) {
        if (data != 0) {
            world.getBlockAt(X + x, y, Z + z).setData(data);
        }
    }
    
    /**
     * Like setblock, but lacks the data != 0 check
     * @param x
     * @param y
     * @param z
     * @param data
     */
    public void setBlockAbs(int x, int y, int z, byte data) {
        world.getBlockAt(X + x, y, Z + z).setData(data);
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
