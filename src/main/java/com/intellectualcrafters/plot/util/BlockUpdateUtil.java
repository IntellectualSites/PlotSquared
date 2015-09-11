package com.intellectualcrafters.plot.util;

import java.util.Collection;

import com.intellectualcrafters.plot.object.ChunkLoc;

public abstract class BlockUpdateUtil
{
    public static BlockUpdateUtil setBlockManager = null;

    public abstract void update(final String worldname, final Collection<ChunkLoc> chunkLocs);
}
