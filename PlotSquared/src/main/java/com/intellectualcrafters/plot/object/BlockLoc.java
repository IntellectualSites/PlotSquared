package com.intellectualcrafters.plot.object;

public class BlockLoc {
    public int x;
    public int y;
    public int z;

    public BlockLoc(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.x;
        result = (prime * result) + this.y;
        result = (prime * result) + this.z;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockLoc other = (BlockLoc) obj;
        return ((this.x == other.x) && (this.y == other.y) && (this.z == other.z));
    }
}
