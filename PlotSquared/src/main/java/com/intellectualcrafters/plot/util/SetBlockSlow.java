package com.intellectualcrafters.plot.util;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

<<<<<<< Updated upstream
public class SetBlockSlow extends AbstractSetBlock {

    @Override
    public boolean set(World world, int x, int y, int z, int id, byte data) {
=======
public class SetBlockSlow extends SetBlockManager {

    @Override
    public void set(World world, int x, int y, int z, int id, byte data) {
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        return false;
=======
>>>>>>> Stashed changes
    }

    @Override
    public void update(List<Chunk> chunks) {
        // TODO Auto-generated method stub
        
    }
    
}
