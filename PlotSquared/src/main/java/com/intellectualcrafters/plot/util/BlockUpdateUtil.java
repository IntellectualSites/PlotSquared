package com.intellectualcrafters.plot.util;

import java.util.List;

import com.intellectualcrafters.plot.object.ChunkLoc;

public abstract class BlockUpdateUtil {
    public static BlockUpdateUtil setBlockManager = null;

    public abstract void update(String worldname, List<ChunkLoc> chunkLocs);
}
