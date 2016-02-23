package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;

public abstract class PlotChunk<T> implements Cloneable {
    private ChunkWrapper chunk;
    private T objChunk;
    
    /**
     * A FaweSections object represents a chunk and the blocks that you wish to change in it.
     */
    public PlotChunk(final ChunkWrapper chunk) {
        this.chunk = chunk;
    }
    
    public void setChunkWrapper(final ChunkWrapper loc) {
        this.chunk = loc;
        this.objChunk = null;
    }
    
    public ChunkWrapper getChunkWrapper() {
        return this.chunk;
    }
    
    public int getX() {
        return chunk.x;
    }
    
    public int getZ() {
        return chunk.z;
    }

    public void addToQueue() {
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk location cannot be null!");
        }
        ((PlotQueue<T>) SetQueue.IMP.queue).setChunk(this);
    }
    
    public void flush(boolean fixLighting) {
        ((PlotQueue<T>) SetQueue.IMP.queue).next(getChunkWrapper(), fixLighting);
    }

    public void fixLighting() {
        ((PlotQueue<T>) SetQueue.IMP.queue).fixLighting(this, true);
    }
    
    public void fill(int id, byte data) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    setBlock(x, y, z, id, data);
                }
            }
        }
    }
    
    public T getChunk() {
        return objChunk != null ? objChunk : getChunkAbs();
    }
    
    public abstract T getChunkAbs();

    public abstract void setBlock(final int x, final int y, final int z, final int id, final byte data);
    
    public void setBlock(int x, int y, int z, PlotBlock block) {
        setBlock(x, y, z, block.id, block.data);
    }
    
    public abstract void setBiome(int x, int z, int biome);

    @Override
    public int hashCode() {
        return chunk.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PlotChunk)) {
            return false;
        }
        return chunk.equals(((PlotChunk) obj).chunk);
    }
    
    @Override
    public String toString() {
        return getChunkWrapper().toString();
    }
    
    @Override
    public abstract PlotChunk clone();
    
    public abstract PlotChunk shallowClone();
}
