package com.plotsquared.bukkit.util;

import java.util.Collection;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SetBlockSlow extends BukkitSetBlockManager
{
    @Override
    public void set(final World world, final int x, final int y, final int z, final int id, final byte data)
    {
        final Block block = world.getBlockAt(x, y, z);
        if (id == -1)
        {
            block.setData(data, false);
            return;
        }
        if (block.getData() == data)
        {
            if (block.getTypeId() != id)
            {
                block.setTypeId(id, false);
            }
        }
        else
        {
            if (block.getTypeId() == id)
            {
                block.setData(data, false);
            }
            else
            {
                block.setTypeIdAndData(id, data, false);
            }
        }
    }

    @Override
    public void update(final Collection<Chunk> chunks)
    {
        // TODO nothing
    }
}
