package com.intellectualcrafters.plot.object;

public class RegionWrapper {
    public final int minX;
    public final int maxX;
    public final int minY;
    public final int maxY;
    public final int minZ;
    public final int maxZ;
    
    public RegionWrapper(final int minX, final int maxX, final int minZ, final int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
        minY = 0;
        maxY = 256;
    }
    
    public RegionWrapper(final int minX, final int maxX, final int minY, final int maxY, final int minZ, final int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    public boolean isIn(final int x, final int y, final int z) {
        return ((x >= minX) && (x <= maxX) && (z >= minZ) && (z <= maxZ) && (y >= minY) && (y <= maxY));
    }
    
    public boolean isIn(final int x, final int z) {
        return ((x >= minX) && (x <= maxX) && (z >= minZ) && (z <= maxZ));
    }
    
    public boolean intersects(RegionWrapper other) {
        return (other.minX <= this.maxX) && (other.maxX >= this.minX) && (other.minY <= this.maxY) && (other.maxY >= this.minY);
    }

    @Override
    public int hashCode() {
        return minX + 13 * maxX + 23 * minZ + 39 * maxZ;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof RegionWrapper) {
            RegionWrapper other = (RegionWrapper) obj;
            return minX == other.minX && minZ == other.minZ && minY == other.minY && maxX == other.maxX && maxZ == other.maxZ && maxY == other.maxY;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return minX + "->" + maxX + "," + minZ + "->" + maxZ;
    }
    
    public Location[] getCorners(String world) {
        Location pos1 = new Location(world, minX, minY, minZ);
        Location pos2 = new Location(world, maxX, maxY, maxZ);
        return new Location[] { pos1, pos2 };
    }
}
