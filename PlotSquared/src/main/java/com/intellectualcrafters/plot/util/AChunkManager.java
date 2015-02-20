package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.List;






import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.RegionWrapper;

public abstract class AChunkManager {
    
    public static AChunkManager manager = null;
    public static RegionWrapper CURRENT_PLOT_CLEAR = null;
    public static HashMap<ChunkLoc, HashMap<Short, Short>> GENERATE_BLOCKS = new HashMap<>();
    public static HashMap<ChunkLoc, HashMap<Short, Byte>> GENERATE_DATA = new HashMap<>();
    
    public static ChunkLoc getChunkChunk(Location loc) {
        int x = loc.getX() >> 9;
        int z = loc.getZ() >> 9;
        return new ChunkLoc(x, z);
    }
    
    public abstract List<ChunkLoc> getChunkChunks(String world);
    
    public abstract void deleteRegionFile(final String world, final ChunkLoc loc);
    
    public abstract Plot hasPlot(String world, ChunkLoc chunk);
    
    public abstract boolean copyRegion(final Location pos1, final Location pos2, final Location newPos, final Runnable whenDone);
    
    public abstract boolean regenerateRegion(final Location pos1, final Location pos2, final Runnable whenDone);
}
