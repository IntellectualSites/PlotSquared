package com.intellectualcrafters.plot.util;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.object.PlotBlock;

public abstract class SetBlockManager {
    public static SetBlockManager setBlockManager = null;
    
    public abstract void set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data);
    
    public abstract void update(List<Chunk> list);
}
