package com.intellectualcrafters.plot.object;

public class BlockLoc {
    public int x;
    public int y;
    public int z;
    
    public float yaw, pitch;
    
    public BlockLoc(final int x, final int y, final int z, final float yaw, final float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public BlockLoc(final int x, final int y, final int z) {
        this(x, y, z, 0f, 0f);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + x;
        result = (prime * result) + y;
        result = (prime * result) + z;
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return x == 0 && y == 0 && z == 0;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockLoc other = (BlockLoc) obj;
        return ((x == other.x) && (y == other.y) && (z == other.z));
    }
    
    @Override
    public String toString() {
        if (x == 0 && y == 0 && z == 0) {
            return "";
        }
        return x + "," + y + "," + z + "," + yaw + "," + pitch;

    }
    
    public static BlockLoc fromString(final String string) {
        final String[] parts = string.split(",");
        
        float yaw, pitch;
        if (parts.length == 3) {
            yaw = 0f;
            pitch = 0f;
        }
        if (parts.length == 5) {
            yaw = Float.parseFloat(parts[3]);
            pitch = Float.parseFloat(parts[4]);
        } else {
            return new BlockLoc(0, 0, 0);
        }
        final int x = Integer.parseInt(parts[0]);
        final int y = Integer.parseInt(parts[1]);
        final int z = Integer.parseInt(parts[2]);
        
        return new BlockLoc(x, y, z, yaw, pitch);
    }
}
