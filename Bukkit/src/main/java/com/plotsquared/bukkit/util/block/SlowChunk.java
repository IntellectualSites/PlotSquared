package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Chunk;

public class SlowChunk extends PlotChunk<Chunk> {

    public PlotBlock[][] result = new PlotBlock[16][];
    public int[][] biomes;
    public SlowChunk(ChunkWrapper chunk) {
        super(chunk);
    }

    @Override
    public Chunk getChunkAbs() {
        ChunkWrapper loc = getChunkWrapper();
        return BukkitUtil.getWorld(loc.world).getChunkAt(loc.x, loc.z);
    }

    @Override
    public void setBiome(int x, int z, int biome) {
        if (biomes == null) {
            biomes = new int[16][16];
        }
        biomes[x][z] = biome;
    }

    @Override
    public void setBlock(int x, int y, int z, int id, byte data) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new PlotBlock[4096];
        }
        result[MainUtil.CACHE_I[x][y][z]][MainUtil.CACHE_J[x][y][z]] = new PlotBlock((short) id, data);
    }

    @Override
    public PlotChunk clone() {
        SlowChunk toReturn = new SlowChunk(getChunkWrapper());
        for (int i = 0; i < result.length; i++) {
            PlotBlock[] matrix = result[i];
            if (matrix != null) {
                toReturn.result[i] = new PlotBlock[matrix.length];
                System.arraycopy(matrix, 0, toReturn.result[i], 0, matrix.length);
            }
        }
        return toReturn;
    }

    @Override
    public PlotChunk shallowClone() {
        SlowChunk toReturn = new SlowChunk(getChunkWrapper());
        toReturn.result = result;
        return toReturn;
    }
}
