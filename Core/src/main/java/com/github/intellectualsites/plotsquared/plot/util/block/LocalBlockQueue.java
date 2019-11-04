package com.github.intellectualsites.plotsquared.plot.util.block;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.sk89q.worldedit.world.block.BlockState;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.Map;

public abstract class LocalBlockQueue {

    /**
     * Needed for compatibility with FAWE.
     *
     * @param world unused
     */
    @Deprecated public LocalBlockQueue(String world) {
    }

    public ScopedLocalBlockQueue getForChunk(int x, int z) {
        int bx = x << 4;
        int bz = z << 4;
        return new ScopedLocalBlockQueue(this, new Location(getWorld(), bx, 0, bz),
            new Location(getWorld(), bx + 15, 255, bz + 15));
    }

    public abstract boolean next();

    public abstract void startSet(boolean parallel);

    public abstract void endSet(boolean parallel);

    public abstract int size();

    public abstract void optimize();

    public abstract long getModified();

    public abstract void setModified(long modified);

    /**
     * Sets the block at the coordinates provided to the given id.
     *
     * @param x the x coordinate from from 0 to 15 inclusive
     * @param y the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z the z coordinate from 0 to 15 inclusive
     * @param id the id to set the block to
     */
    public abstract boolean setBlock(final int x, final int y, final int z, final BlockState id);

    public abstract boolean setBlock(final int x, final int y, final int z, final BaseBlock id);

    public boolean setTile(int x, int y, int z, CompoundTag tag) {
        SchematicHandler.manager.restoreTile(this, tag, x, y, z);
        return true;
    }

    public abstract BlockState getBlock(int x, int y, int z);

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
        BlockVector2 loc = BlockVector2.at(x, z);
        for (Map.Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Location pLoc = pp.getLocation();
            if (!StringMan.isEqual(getWorld(), pLoc.getWorld()) || !pLoc.getBlockVector2()
                .equals(loc)) {
                continue;
            }
            pLoc.setY(WorldUtil.IMP.getHighestBlock(getWorld(), pLoc.getX(), pLoc.getZ()));
            pp.teleport(pLoc);
        }
    }

    public boolean enqueue() {
        return GlobalBlockQueue.IMP.enqueue(this);
    }

    public void setCuboid(Location pos1, Location pos2, BlockState block) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.min(255, Math.max(pos1.getY(), pos2.getY()));
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBlock(x, y, z, block);
                }
            }
        }
    }

    public void setCuboid(Location pos1, Location pos2, BlockBucket blocks) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.min(255, Math.max(pos1.getY(), pos2.getY()));
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBlock(x, y, z, blocks.getBlock());
                }
            }
        }
    }
}
