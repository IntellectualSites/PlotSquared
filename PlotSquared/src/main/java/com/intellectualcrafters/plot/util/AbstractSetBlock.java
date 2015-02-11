package com.intellectualcrafters.plot.util;

import java.util.ArrayList;

import org.bukkit.Chunk;

public abstract class AbstractSetBlock {
    public static AbstractSetBlock setBlockManager = null;
    
    public abstract boolean set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data);
    
    public abstract void update(ArrayList<Chunk> chunks);
}
