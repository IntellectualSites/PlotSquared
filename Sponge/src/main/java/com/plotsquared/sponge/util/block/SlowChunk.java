package com.plotsquared.sponge.util.block;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.world.Chunk;

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
        if (this.biomes == null) {
            this.biomes = new int[16][16];
        }
        this.biomes[x][z] = biome;
    }
    
    @Override
    public void setBlock(int x, int y, int z, int id, byte data) {
        if (this.result[y >> 4] == null) {
            this.result[y >> 4] = new PlotBlock[4096];
        }
        if (id == this.lastBlock.id && data == this.lastBlock.data) {
            this.result[MainUtil.CACHE_I[x][y][z]][MainUtil.CACHE_J[x][y][z]] = this.lastBlock;
        } else {
            this.result[MainUtil.CACHE_I[x][y][z]][MainUtil.CACHE_J[x][y][z]] = PlotBlock.get((short) id, data);
        }
    }
    
    @Override
    public PlotChunk clone() {
        SlowChunk toReturn = new SlowChunk(getChunkWrapper());
        for (int i = 0; i < this.result.length; i++) {
            PlotBlock[] matrix = this.result[i];
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
        toReturn.result = this.result;
        return toReturn;
    }
}
