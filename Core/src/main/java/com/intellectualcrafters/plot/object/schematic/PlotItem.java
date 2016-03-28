package com.intellectualcrafters.plot.object.schematic;

public class PlotItem {
    public int x;
    public int y;
    public int z;
    public short[] id;
    public byte[] data;
    public byte[] amount;

    public PlotItem(short x, short y, short z, short[] id, byte[] data, byte[] amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
        this.amount = amount;
    }
}
