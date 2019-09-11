package com.github.intellectualsites.plotsquared.plot.object;

public class ChunkLoc {

    public int x;
    public int z;

    public ChunkLoc(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public static long getChunkLong(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }

    public long toLong() {
        return getChunkLong(this.x,this.z);
    }

    public static int getX(long chunkLong) {
        return (int)(chunkLong & 0xffffffffL);
    }

    public static int getZ(long chunkLong) {
        return (int)(chunkLong >>> 32 & 0xffffffffL);
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
