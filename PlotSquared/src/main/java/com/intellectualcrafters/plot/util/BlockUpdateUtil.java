package com.intellectualcrafters.plot.util;

import java.util.List;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.bukkit.BukkitSetBlockManager;

public abstract class BlockUpdateUtil {
    public static BlockUpdateUtil setBlockManager = null;

    public abstract void update(String worldname, List<ChunkLoc> chunkLocs);
}
