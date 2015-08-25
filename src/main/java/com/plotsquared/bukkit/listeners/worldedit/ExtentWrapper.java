package com.plotsquared.bukkit.listeners.worldedit;

import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

public class ExtentWrapper extends AbstractDelegateExtent {

    protected ExtentWrapper(Extent extent) {
        super(extent);
    }
    
}
