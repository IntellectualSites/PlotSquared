package com.intellectualcrafters.plot.object;


public class RegionWrapper {
    public final int minX;
    public final int maxX;
    public final int minZ;
    public final int maxZ;
    
    public RegionWrapper(int minX, int maxX, int minZ, int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
    }
}
