package com.github.intellectualsites.plotsquared.plot.object.schematic;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;

public class PlotItem {

    public final int x;
    public final int y;
    public final int z;
    // public final short[] id;
    // public final byte[] data;
    public final ItemType[] types;
    public final byte[] amount;

    public PlotItem(short x, short y, short z, ItemType[] types, byte[] amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.types = types;
        this.amount = amount;
    }
}
