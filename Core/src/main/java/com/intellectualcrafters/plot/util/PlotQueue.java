package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;

import java.util.Collection;

public interface PlotQueue<T> {
    boolean setBlock(final String world, final int x, final int y, final int z, final short id, final byte data);
    
    PlotChunk<T> getChunk(ChunkWrapper wrap);
    
    void setChunk(PlotChunk<T> chunk);
    
    boolean fixLighting(PlotChunk<T> chunk, boolean fixAll);
    
    void sendChunk(String world, Collection<ChunkLoc> locs);

    /**
     * Gets the FaweChunk and sets the requested blocks
     * @return
     */
    PlotChunk<T> next();
    
    PlotChunk<T> next(ChunkWrapper wrap, boolean fixLighting);

    void clear();
}

