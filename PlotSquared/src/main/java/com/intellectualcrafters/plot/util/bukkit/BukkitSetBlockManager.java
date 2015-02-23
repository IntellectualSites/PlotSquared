package com.intellectualcrafters.plot.util.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.BlockUpdateUtil;

public abstract class BukkitSetBlockManager extends BlockUpdateUtil {
    public static BukkitSetBlockManager setBlockManager = null;

    public abstract void set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data);

    public abstract void update(List<Chunk> list);

    @Override
    public void update(final String worldname, final List<ChunkLoc> chunkLocs) {
        final World world = BukkitUtil.getWorld(worldname);
        final ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        for (final ChunkLoc loc : chunkLocs) {
            chunks.add(world.getChunkAt(loc.x, loc.z));
        }
        setBlockManager.update(chunks);
    }
}
