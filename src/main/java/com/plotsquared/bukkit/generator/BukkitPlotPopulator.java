package com.plotsquared.bukkit.generator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.SetBlockQueue;

public abstract class BukkitPlotPopulator extends BlockPopulator {
    
    private final PseudoRandom random = new PseudoRandom();
    
    public int X;
    public int Z;
    public String worldname;
    private Chunk chunk;
    
    @Override
    public void populate(final World world, final Random rand, final Chunk chunk) {
        try {
            this.chunk = chunk;
            worldname = world.getName();
            X = this.chunk.getX() << 4;
            Z = this.chunk.getZ() << 4;
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
                for (final Entry<PlotLoc, HashMap<Short, Byte>> entry : ChunkManager.GENERATE_DATA.entrySet()) {
                    for (final Entry<Short, Byte> entry2 : entry.getValue().entrySet()) {
                        loc = entry.getKey();
                        final int xx = loc.x - X;
                        final int zz = loc.z - Z;
                        if ((xx >= 0) && (xx < 16)) {
                            if ((zz >= 0) && (zz < 16)) {
                                setBlock(xx, entry2.getKey(), zz, entry2.getValue());
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public abstract void populate(final World world, final RegionWrapper requiredRegion, final PseudoRandom random, final int cx, final int cz);
    
    /**
     * Set the id and data at a location. (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param id
     * @param data
     */
    public void setBlock(final int x, final int y, final int z, final short id, final byte data) {
        if (data == 0) {
            SetBlockQueue.setBlock(worldname, x, y, z, id);
        } else {
            SetBlockQueue.setBlock(worldname, x, y, z, new PlotBlock(id, data));
        }
    }
    
    /**
     * Set the data at a location. (x, y, z) must be between [0,15], [0,255], [0,15]
     * @param x
     * @param y
     * @param z
     * @param data
     */
    public void setBlock(final int x, final int y, final int z, final byte data) {
        if (data != 0) {
            chunk.getBlock(x, y, z).setData(data);
        }
    }
    
    /**
     * Like setblock, but lacks the data != 0 check
     * @param x
     * @param y
     * @param z
     * @param data
     */
    public void setBlockAbs(final int x, final int y, final int z, final byte data) {
        chunk.getBlock(x, y, z).setData(data);
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
