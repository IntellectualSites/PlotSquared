package com.intellectualcrafters.plot.object.schematic;

public class PlotItem {
    public int x;
    public int y;
    public int z;
    public short[] id;
    public byte[] data;
    public byte[] amount;
    
    public PlotItem(final short x, final short y, final short z, final short[] id, final byte[] data, final byte[] amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
        this.amount = amount;
    }
}
