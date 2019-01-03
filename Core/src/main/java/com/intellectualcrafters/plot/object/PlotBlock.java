package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.config.Settings;

public class PlotBlock {

    public static final PlotBlock EVERYTHING = new PlotBlock((short) 0, (byte) 0);
    private static final PlotBlock[] CACHE = new PlotBlock[65535];
    static {
        for (int i = 0; i < 65535; i++) {
            short id = (short) (i >> 4);
            byte data = (byte) (i & 15);
            CACHE[i] = new PlotBlock(id, data);
        }
    }

    public final short id;
    public final byte data;
    public PlotBlock(short id, byte data) {
        this.id = id;
        this.data = data;
    }

    public static PlotBlock get(char combinedId) {
        switch (combinedId) {
            case 0:
                return null;
            case 1:
                return get(0, 0);
            default:
                return get(combinedId >> 4, combinedId & 15);
        }
    }

    public static PlotBlock get(int id, int data) {
        return Settings.Enabled_Components.BLOCK_CACHE && data > 0 ? CACHE[(id << 4) + data] : new PlotBlock((short) id, (byte) data);
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
