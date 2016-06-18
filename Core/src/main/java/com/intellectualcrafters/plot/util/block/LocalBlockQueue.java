package com.intellectualcrafters.plot.util.block;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import java.util.Map;

public abstract class LocalBlockQueue {

    public LocalBlockQueue(String world) {
        // Implement this elsewhere
    }

    public ScopedLocalBlockQueue getForChunk(int x, int z) {
        int bx = x << 4;
        int bz = z << 4;
        ScopedLocalBlockQueue scoped = new ScopedLocalBlockQueue(this, new Location(getWorld(), bx, 0, bz), new Location(getWorld(), bx + 15, 255, bz + 15));
    }

    public abstract boolean next();

    public abstract void startSet(boolean parallel);

    public abstract void endSet(boolean parallel);

    public abstract int size();

    public abstract void optimize();

    public abstract void setModified(long modified);

    public abstract long getModified();

    public abstract boolean setBlock(final int x, final int y, final int z, final int id, final int data);

    public final boolean setBlock(int x, int y, int z, int id) {
        return setBlock(x, y, z, id, 0);
    }

    public final boolean setBlock(int x, int y, int z, PlotBlock block) {
        return setBlock(x, y, z, block.id, block.data);
    }

    public boolean setTile(int x, int y, int z, CompoundTag tag) {
        SchematicHandler.manager.restoreTile(this, tag, x, y, z);
        return true;
    }

    public abstract PlotBlock getBlock(int x, int y, int z);

    public abstract boolean setBiome(int x, int z, String biome);

    public abstract String getWorld();

    public abstract void flush();

    public final void setModified() {
        setModified(System.currentTimeMillis());
    }

    public abstract void refreshChunk(int x, int z);

    public abstract void fixChunkLighting(int x, int z);

    public abstract void regenChunk(int x, int z);

    public final void regenChunkSafe(int x, int z) {
        regenChunk(x, z);
        fixChunkLighting(x, z);
        ChunkLoc loc = new ChunkLoc(x, z);
        for (Map.Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Location pLoc = pp.getLocation();
            if (!StringMan.isEqual(getWorld(), pLoc.getWorld()) || !pLoc.getChunkLoc().equals(loc)) {
                continue;
            }
            pLoc.setY(WorldUtil.IMP.getHighestBlock(getWorld(), pLoc.getX(), pLoc.getZ()));
            pp.teleport(pLoc);
        }
    }

    public void enqueue() {
        GlobalBlockQueue.IMP.enqueue(this);
    }

    public final void setCuboid(Location pos1, Location pos2, PlotBlock block) {
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    setBlock(x, y, z, block);
                }
            }
        }
    }

    public final void setCuboid(Location pos1, Location pos2, PlotBlock[] blocks) {
        for (int y = pos1.getY(); y <= Math.min(255, pos2.getY()); y++) {
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    int i = PseudoRandom.random.random(blocks.length);
                    PlotBlock block = blocks[i];
                    setBlock(x, y, z, block);
                }
            }
        }
    }
}
