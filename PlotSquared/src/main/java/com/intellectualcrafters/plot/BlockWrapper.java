package com.intellectualcrafters.plot;


public class BlockWrapper {
    public int  x;
    public int  y;
    public int  z;
    public int  id;
    public byte data;

    public BlockWrapper(final int x, final int y, final int z, final short id, final byte data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }
}
