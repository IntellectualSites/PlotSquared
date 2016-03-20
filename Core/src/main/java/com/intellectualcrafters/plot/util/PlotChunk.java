package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;

/**
 * The PlotChunk class is primarily used for world generation and mass block placement.<br>
 *  - With mass block placement, it is associated with a queue<br>
 *  - World Generation has no queue, so don't use those methods in that case
 * @param <T>
 */
public abstract class PlotChunk<T> implements Cloneable {
    private ChunkWrapper chunk;
    private T objChunk;
    
    /**
     * A FaweSections object represents a chunk and the blocks that you wish to change in it.
     */
    public PlotChunk(final ChunkWrapper chunk) {
        this.chunk = chunk;
    }

    public ChunkWrapper getChunkWrapper() {
        return this.chunk;
    }
    
    public void setChunkWrapper(final ChunkWrapper loc) {
        this.chunk = loc;
        this.objChunk = null;
    }
    
    public int getX() {
        return chunk.x;
    }
    
    public int getZ() {
        return chunk.z;
    }

    /**
     * Adds this PlotChunk to the SetQueue for later block placement<br>
     *  - Will cause issues if not the right type for the implementation
     */
    public void addToQueue() {
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk location cannot be null!");
        }
        ((PlotQueue<T>) SetQueue.IMP.queue).setChunk(this);
    }

    /**
     * Force the queue to finish processing this chunk
     * @param fixLighting
     */
    public void flush(boolean fixLighting) {
        ((PlotQueue<T>) SetQueue.IMP.queue).next(getChunkWrapper(), fixLighting);
    }

    /**
     * Force the queue to fix lighting for this chunk
     */
    public void fixLighting() {
        ((PlotQueue<T>) SetQueue.IMP.queue).fixLighting(this, true);
    }

    /**
     * Fill this chunk with a block
     * @param id
     * @param data
     */
    public void fill(int id, byte data) {
        fillCuboid(0, 15, 0, 255, 0, 15, id, data);
    }

    /**
     * Fill this chunk with blocks (random)
     * @param blocks
     */
    public void fill(PlotBlock[] blocks) {
        fillCuboid(0, 15, 0, 255, 0, 15, blocks);
    }

    /**
     * Fill a cuboid in this chunk with a block
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @param id
     * @param data
     */
    public void fillCuboid(int x1, int x2, int y1, int y2, int z1, int z2, int id, byte data) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    setBlock(x, y, z, id, data);
                }
            }
        }
    }

    /**
     * Fill a cuboid in this chunk with blocks
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @param blocks
     */
    public void fillCuboid(int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock[] blocks) {
        if (blocks.length == 1) {
            fillCuboid(x1, x2, y1, y2, z1, z2, blocks[0]);
            return;
        }
        if (chunk != null) {
            PseudoRandom.random.state = (chunk.x << 16) | (chunk.z & 0xFFFF);
        }
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    setBlock(x, y, z, blocks[PseudoRandom.random.random(blocks.length)]);
                }
            }
        }
    }

    /**
     * Fill a cuboid in this chunk with a block
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @param block
     */
    public void fillCuboid(int x1, int x2, int y1, int y2, int z1, int z2, PlotBlock block) {
        fillCuboid(x1, x2, y1, y2, z1, z2, block.id, block.data);
    }

    /**
     * Get the implementation specific chunk
     * @Nullable If no location is tied to this container
     * @return Chunk
     */
    public T getChunk() {
        return objChunk != null ? objChunk : getChunkAbs();
    }

    /**
     * Get the implementation specific chunk (no caching)
     * @return
     */
    public abstract T getChunkAbs();

    /**
     * Set a block in this container
     * @param x
     * @param y
     * @param z
     * @param id
     * @param data
     */
    public abstract void setBlock(final int x, final int y, final int z, final int id, final byte data);

    /**
     * Set a block in this container
     * @param x
     * @param y
     * @param z
     * @param block
     */
    public void setBlock(int x, int y, int z, PlotBlock block) {
        setBlock(x, y, z, block.id, block.data);
    }

    /**
     * Set a biome in this container
     * @param x
     * @param z
     * @param biome
     */
    public abstract void setBiome(int x, int z, int biome);

    @Override
    public int hashCode() {
        return chunk.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlotChunk)) {
            return false;
        }
        return chunk.equals(((PlotChunk) obj).chunk);
    }
    
    @Override
    public String toString() {
        return getChunkWrapper().toString();
    }

    /**
     * Attempt to clone this PlotChunk object<br>
     *  - Depending on the implementation, this may not work
     * @return
     */
    @Override
    public abstract PlotChunk clone();

    /**
     * Attempt a shallow clone i.e. block mappings share the same reference<br>
     *  - Depending on the implementation, this may not work
     * @return
     */
    public abstract PlotChunk shallowClone();
}
