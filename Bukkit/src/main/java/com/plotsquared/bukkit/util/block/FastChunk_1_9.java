package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Chunk;

public class FastChunk_1_9 extends PlotChunk<Chunk> {

    public FastChunk_1_9(SetQueue.ChunkWrapper wrap) {
        super(wrap);
    }

    @Override
    public Chunk getChunkAbs() {
        SetQueue.ChunkWrapper loc = getChunkWrapper();
        return BukkitUtil.getWorld(loc.world).getChunkAt(loc.x, loc.z);
    }

    @Override public void setBlock(int x, int y, int z, int id, byte data) {

    }

    @Override public void setBiome(int x, int z, int biome) {

    }

    @Override public PlotChunk clone() {
        return null;
    }

    @Override public PlotChunk shallowClone() {
        return null;
    }
}
