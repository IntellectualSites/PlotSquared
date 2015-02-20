package com.intellectualcrafters.plot.util;

import java.util.List;

import com.intellectualcrafters.plot.object.ChunkLoc;

public abstract class AbstractSetBlock {
    public static AbstractSetBlock setBlockManager = null;
    
    public abstract boolean set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data);
    
    public abstract void update(String world, List<ChunkLoc> chunks);
}
