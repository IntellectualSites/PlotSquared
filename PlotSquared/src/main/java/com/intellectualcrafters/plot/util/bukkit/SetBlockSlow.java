package com.intellectualcrafters.plot.util.bukkit;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SetBlockSlow extends SetBlockManager {

    @Override
    public void set(World world, int x, int y, int z, int id, byte data) {

        Block block = world.getBlockAt(x, y, z);
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
    public void update(List<Chunk> chunks) {
        // TODO Auto-generated method stub
    }
    
}
