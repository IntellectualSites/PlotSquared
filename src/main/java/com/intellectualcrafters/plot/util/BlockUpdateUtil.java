package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ChunkLoc;

import java.util.Collection;

public abstract class BlockUpdateUtil {
    public static BlockUpdateUtil setBlockManager = null;

    public abstract void update(String worldname, Collection<ChunkLoc> chunkLocs);
}
