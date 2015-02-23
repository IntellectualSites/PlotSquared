package com.intellectualcrafters.plot.util;

import java.util.List;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.bukkit.SetBlockManager;

public abstract class AbstractSetBlock {
    public static SetBlockManager setBlockManager = null;

    public abstract void update(String worldname, List<ChunkLoc> chunkLocs);
}
