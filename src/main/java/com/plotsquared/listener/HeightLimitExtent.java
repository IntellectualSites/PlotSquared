package com.plotsquared.listener;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

public class HeightLimitExtent extends AbstractDelegateExtent
{

    private final int max;
    private final int min;

    public HeightLimitExtent(final int min, final int max, final Extent child)
    {
        super(child);
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean setBlock(final Vector location, final BaseBlock block) throws WorldEditException
    {
        final int y = location.getBlockY();
        if ((y < min) || (y > max)) { return false; }
        return super.setBlock(location, block);
    }

}
