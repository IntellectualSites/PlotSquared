package com.intellectualcrafters.plot.util.bukkit;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SetBlockSlow extends SetBlockManager {
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
    public void update(final List<Chunk> chunks) {
        // TODO Auto-generated method stub
    }
}
