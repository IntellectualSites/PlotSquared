package com.plotsquared.bukkit.listeners.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

public class HeightLimitExtent extends AbstractDelegateExtent {

    private int max;
    private int min;

    public HeightLimitExtent(int min, int max, Extent child) {
        super(child);
        this.min = min;
        this.max = max;
    }
    
    
    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        int y = location.getBlockY();
        if (y < min || y > max) {
            return false;
        }
        return super.setBlock(location, block);
    }
    
    
}
