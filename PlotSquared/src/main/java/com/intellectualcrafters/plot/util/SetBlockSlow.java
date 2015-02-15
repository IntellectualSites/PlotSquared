package com.intellectualcrafters.plot.util;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SetBlockSlow extends AbstractSetBlock {

    @Override
    public boolean set(World world, int x, int y, int z, int id, byte data) {
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
        return false;
    }

    @Override
    public void update(List<Chunk> chunks) {
        // TODO Auto-generated method stub
        
    }
    
}
