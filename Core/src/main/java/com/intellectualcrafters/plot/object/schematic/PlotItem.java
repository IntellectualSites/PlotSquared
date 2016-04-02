package com.intellectualcrafters.plot.object.schematic;

public class PlotItem {

    public final int x;
    public final int y;
    public final int z;
    public final short[] id;
    public final byte[] data;
    public final byte[] amount;

    public PlotItem(short x, short y, short z, short[] id, byte[] data, byte[] amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
        this.amount = amount;
    }
}
