package com.plotsquared.core.listener;

import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;

public class ExtentWrapper extends AbstractDelegateExtent {

    protected ExtentWrapper(final Extent extent) {
        super(extent);
    }

}
