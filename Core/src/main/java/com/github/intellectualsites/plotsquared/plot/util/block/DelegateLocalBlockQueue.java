package com.github.intellectualsites.plotsquared.plot.util.block;

import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.sk89q.worldedit.world.block.BaseBlock;

public class DelegateLocalBlockQueue extends LocalBlockQueue {

    private final LocalBlockQueue parent;

    public DelegateLocalBlockQueue(LocalBlockQueue parent) {
        super(parent == null ? null : parent.getWorld());
        this.parent = parent;
    }

    public LocalBlockQueue getParent() {
        return parent;
    }

    @Override public boolean next() {
        return parent.next();
    }

    @Override public void startSet(boolean parallel) {
        if (parent != null) {
            parent.startSet(parallel);
        }
    }

    @Override public void endSet(boolean parallel) {
        if (parent != null) {
            parent.endSet(parallel);
        }
    }

    @Override public int size() {
        if (parent != null) {
            return parent.size();
        }
        return 0;
    }

    @Override public void optimize() {
        if (parent != null) {
            parent.optimize();
        }
    }

    @Override public long getModified() {
        if (parent != null) {
            return parent.getModified();
        }
        return 0;
    }

    @Override public void setModified(long modified) {
        if (parent != null) {
            parent.setModified(modified);
        }
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        return parent.setBlock(x, y, z, id);
    }

    @Override public boolean setBlock(int x, int y, int z, String id) {
        return parent.setBlock(x, y, z, id);
    }

    @Override public boolean setBlock(int x, int y, int z, int id, int data) {
        return parent.setBlock(x, y, z, id, data);
    }

    @Override public PlotBlock getBlock(int x, int y, int z) {
        return parent.getBlock(x, y, z);
    }

    @Override public boolean setBiome(int x, int y, String biome) {
        return parent.setBiome(x, y, biome);
    }

    @Override public String getWorld() {
        return parent.getWorld();
    }

    @Override public void flush() {
        if (parent != null) {
            parent.flush();
        }
    }

    @Override public void refreshChunk(int x, int z) {
        if (parent != null) {
            parent.refreshChunk(x, z);
        }
    }

    @Override public void fixChunkLighting(int x, int z) {
        if (parent != null) {
            parent.fixChunkLighting(x, z);
        }
    }

    @Override public void regenChunk(int x, int z) {
        if (parent != null) {
            parent.regenChunk(x, z);
        }
    }

    @Override public void enqueue() {
        if (parent != null) {
            parent.enqueue();
        }
    }
}
