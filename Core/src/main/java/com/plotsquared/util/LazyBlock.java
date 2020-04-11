package com.plotsquared.util;

import com.sk89q.worldedit.world.block.BlockState;

public abstract class LazyBlock {

    public abstract BlockState getBlockState();

    public String getId() {
        return getBlockState().toString();
    }
}
