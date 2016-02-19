package com.plotsquared.sponge.util.block;

import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;

import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;

public class GenChunk extends PlotChunk<Chunk> {
    
    public boolean modified = false;
    private final MutableBlockVolume terain;
    private final MutableBiomeArea biome;
    private final int bz;
    private final int bx;
    
    public GenChunk(MutableBlockVolume terain, MutableBiomeArea biome, ChunkWrapper wrap) {
        super(wrap);
        this.bx = wrap.x << 4;
        this.bz = wrap.z << 4;
        this.terain = terain;
        this.biome = biome;
    }
    
    @Override
    public Chunk getChunkAbs() {
        ChunkWrapper wrap = getChunkWrapper();
        return SpongeUtil.getWorld(wrap.world).getChunk(wrap.x << 4, 0, wrap.z << 4).orElse(null);
    }
    
    @Override
    public void setBiome(int x, int z, int biome) {
        if (this.biome != null) {
            this.biome.setBiome(bx + x, bz + z, SpongeUtil.getBiome(biome));
        }
    }
    
    @Override
    public void setBlock(int x, int y, int z, int id, byte data) {
        terain.setBlock(bx + x, y, bz + z, SpongeUtil.getBlockState(id, data));
    }
    
    @Override
    public PlotChunk clone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public PlotChunk shallowClone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
