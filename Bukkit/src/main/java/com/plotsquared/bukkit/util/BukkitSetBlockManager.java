package com.plotsquared.bukkit.util;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.BlockUpdateUtil;
import com.intellectualcrafters.plot.util.MainUtil;

public abstract class BukkitSetBlockManager extends BlockUpdateUtil {
    public static BukkitSetBlockManager setBlockManager = null;
    
    public abstract void set(final org.bukkit.World world, final int x, final int y, final int z, final int blockId, final byte data);
    
    public abstract void update(final Collection<Chunk> list);
    
    @Override
    public void update(final String worldname, final Collection<ChunkLoc> chunkLocs) {
        final ArrayList<Chunk> chunks = new ArrayList<>();
        final World world = BukkitUtil.getWorld(worldname);
        MainUtil.objectTask(chunkLocs, new RunnableVal<ChunkLoc>() {
            
            @Override
            public void run() {
                chunks.add(world.getChunkAt(value.x, value.z));
            }
        }, new Runnable() {
            
            @Override
            public void run() {
                setBlockManager.update(chunks);
            }
        });
    }
}
