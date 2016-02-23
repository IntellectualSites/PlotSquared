package com.plotsquared.sponge.util.block;

import org.spongepowered.api.world.Chunk;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;

public class SlowChunk extends PlotChunk<Chunk> {
    
    public PlotBlock[][] result = new PlotBlock[16][];
    public int[][] biomes;
    private PlotBlock lastBlock;
    
    public SlowChunk(ChunkWrapper chunk) {
        super(chunk);
    }
    
    @Override
    public Chunk getChunkAbs() {
        ChunkWrapper loc = getChunkWrapper();
        return SpongeUtil.getWorld(loc.world).getChunk(loc.x << 4, 0, loc.z << 4).get();
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
        if (id == lastBlock.id && data == lastBlock.data) {
            result[MainUtil.CACHE_I[x][y][z]][MainUtil.CACHE_J[x][y][z]] = lastBlock;
        } else {
            result[MainUtil.CACHE_I[x][y][z]][MainUtil.CACHE_J[x][y][z]] = new PlotBlock((short) id, data);
        }
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
