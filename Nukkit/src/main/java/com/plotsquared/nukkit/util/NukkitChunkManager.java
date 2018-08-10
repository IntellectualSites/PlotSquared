package com.plotsquared.nukkit.util;


import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.ChunkManager;

public class NukkitChunkManager extends ChunkManager {
    public NukkitChunkManager() {
        PS.debug("Not implemented: NukkitChunkManager");
    }

    @Override public int[] countEntities(Plot plot) {
        return new int[0];
    }

    @Override public boolean loadChunk(String world, ChunkLoc loc, boolean force) {
        return true;
    }

    @Override public void unloadChunk(String world, ChunkLoc loc, boolean save, boolean safe) {

    }

    @Override
    public boolean copyRegion(Location pos1, Location pos2, Location newPos, Runnable whenDone) {
        return false;
    }

    @Override public boolean regenerateRegion(Location pos1, Location pos2, boolean ignoreAugment,
        Runnable whenDone) {
        return false;
    }

    @Override public void clearAllEntities(Location pos1, Location pos2) {

    }

    @Override public void swap(Location bot1, Location top1, Location bot2, Location top2,
        Runnable whenDone) {
        whenDone.run();
    }
}
