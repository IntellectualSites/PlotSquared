package com.intellectualcrafters.plot.util;

import java.util.Collection;

import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;

public interface PlotQueue<T> {
    public boolean setBlock(final String world, final int x, final int y, final int z, final short id, final byte data);
    
    public PlotChunk<T> getChunk(ChunkWrapper wrap);
    
    public void setChunk(PlotChunk<T> chunk);
    
    public boolean fixLighting(PlotChunk<T> chunk, boolean fixAll);
    
    public void sendChunk(String world, Collection<ChunkLoc> locs);

    /**
     * Gets the FaweChunk and sets the requested blocks
     * @return
     */
    public PlotChunk<T> next();
    
    public PlotChunk<T> next(ChunkWrapper wrap, boolean fixLighting);

    public void clear();
}

