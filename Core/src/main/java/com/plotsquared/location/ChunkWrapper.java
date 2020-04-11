package com.plotsquared.location;

import com.plotsquared.util.MathMan;
import com.plotsquared.util.StringMan;

public class ChunkWrapper {

    public final int x;
    public final int z;
    public final String world;

    public ChunkWrapper(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override public int hashCode() {
        return MathMan.pair((short) x, (short) z);
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChunkWrapper other = (ChunkWrapper) obj;
        return (this.x == other.x) && (this.z == other.z) && StringMan
            .isEqual(this.world, other.world);
    }

    @Override public String toString() {
        return this.world + ":" + this.x + "," + this.z;
    }
}
