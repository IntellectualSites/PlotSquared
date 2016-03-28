package com.intellectualcrafters.plot.object;

public class BlockLoc {

    public final int x;
    public final int y;
    public final int z;

    public final float yaw;
    public final float pitch;

    public BlockLoc(int x, int y, int z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public BlockLoc(int x, int y, int z) {
        this(x, y, z, 0f, 0f);
    }

    public static BlockLoc fromString(String string) {
        String[] parts = string.split(",");

        float yaw;
        float pitch;
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
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);

        return new BlockLoc(x, y, z, yaw, pitch);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.x;
        result = prime * result + this.y;
        result = prime * result + this.z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return this.x == 0 && this.y == 0 && this.z == 0;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlockLoc other = (BlockLoc) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public String toString() {
        if (this.x == 0 && this.y == 0 && this.z == 0) {
            return "";
        }
        return this.x + "," + this.y + "," + this.z + "," + this.yaw + "," + this.pitch;

    }
}
