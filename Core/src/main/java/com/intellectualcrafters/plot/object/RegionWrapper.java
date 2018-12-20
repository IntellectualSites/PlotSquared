package com.intellectualcrafters.plot.object;

public class RegionWrapper {
    public final int minX;
    public final int maxX;
    public final int minY;
    public final int maxY;
    public final int minZ;
    public final int maxZ;

    public RegionWrapper(int minX, int maxX, int minZ, int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
        this.minY = 0;
        this.maxY = 256;
    }

    public RegionWrapper(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
        this.minY = minY;
        this.maxY = maxY;
    }

    public boolean isIn(int x, int y, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
    }

    public boolean isIn(int x, int z) {
        return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
    }
    
    public boolean intersects(RegionWrapper other) {
        return other.minX <= this.maxX && other.maxX >= this.minX && other.minY <= this.maxY && other.maxY >= this.minY;
    }

    @Override
    public int hashCode() {
        return this.minX + 13 * this.maxX + 23 * this.minZ + 39 * this.maxZ;
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
            return this.minX == other.minX && this.minZ == other.minZ && this.minY == other.minY && this.maxX == other.maxX && this.maxZ == other.maxZ
                    && this.maxY == other.maxY;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return this.minX + "->" + this.maxX + "," + this.minZ + "->" + this.maxZ;
    }
    
    public Location[] getCorners(String world) {
        Location pos1 = new Location(world, this.minX, this.minY, this.minZ);
        Location pos2 = new Location(world, this.maxX, this.maxY, this.maxZ);
        return new Location[] { pos1, pos2 };
    }
}
