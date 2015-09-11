package com.intellectualcrafters.plot.object;

public class ChunkLoc
{
    public int x;
    public int z;

    public ChunkLoc(final int x, final int z)
    {
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + x;
        result = (prime * result) + z;
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final ChunkLoc other = (ChunkLoc) obj;
        return ((x == other.x) && (z == other.z));
    }
}
