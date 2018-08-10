package com.intellectualcrafters.plot.object;

public class ChunkLoc {

    public int x;
    public int z;

    public ChunkLoc(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override public int hashCode() {
        return (x << 16) | (z & 0xFFFF);
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChunkLoc other = (ChunkLoc) obj;
        return (this.x == other.x) && (this.z == other.z);
    }

    @Override public String toString() {
        return this.x + "," + this.z;
    }
}
