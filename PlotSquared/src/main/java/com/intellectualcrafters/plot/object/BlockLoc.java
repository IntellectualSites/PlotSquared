package com.intellectualcrafters.plot.object;

import org.bukkit.Location;
import org.bukkit.World;


public class BlockLoc {
    public int x;
    public int y;
    public int z;
    
    public BlockLoc(int x, int y, int z) {
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlockLoc other = (BlockLoc) obj;
        return ((this.x == other.x) && (this.y == other.y) && (this.z == other.z));
    }
    
}
