package com.intellectualcrafters.plot.object;

public class PlotBlock {

    public static final PlotBlock EVERYTHING = new PlotBlock((short) 0, (byte) 0);

    public final short id;
    public final byte data;

    public PlotBlock(short id, byte data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlotBlock other = (PlotBlock) obj;
        return (this.id == other.id) && ((this.data == other.data) || (this.data == -1) || (other.data == -1));
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        if (this.data == -1) {
            return this.id + "";
        }
        return this.id + ":" + this.data;
    }
}
