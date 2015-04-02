package com.intellectualcrafters.plot.util.bukkit;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.intellectualcrafters.plot.util.MainUtil;

public class SetBlockSlow extends BukkitSetBlockManager {
    @Override
    public void set(final World world, final int x, final int y, final int z, final int id, final byte data) {
        final Block block = world.getBlockAt(x, y, z);
        if (block.getData() == data) {
            if (block.getTypeId() != id) {
                block.setTypeId(id, false);
            }
        } else {
            if (block.getTypeId() == id) {
                block.setData(data, false);
            } else {
                block.setTypeIdAndData(id, data, false);
            }
        }
    }

    @Override
    public void update(final Collection<Chunk> chunks) {
        if (MainUtil.canSendChunk) {
            try {
                SendChunk.sendChunk(chunks);
            } catch (final Throwable e) {
                MainUtil.canSendChunk = false;
            }
        }
        else {
            for (Chunk chunk : chunks) {
                chunk.unload();
                chunk.load(true);
            }
        }
    }
}
