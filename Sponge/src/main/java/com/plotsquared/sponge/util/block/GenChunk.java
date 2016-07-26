package com.plotsquared.sponge.util.block;

import com.intellectualcrafters.plot.object.ChunkWrapper;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.block.ScopedLocalBlockQueue;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;

public class GenChunk extends ScopedLocalBlockQueue {

    private final MutableBlockVolume terrain;
    private final MutableBiomeArea biome;
    private final int bz;
    private final int bx;
    private final String world;

    public boolean modified = false;

    public GenChunk(MutableBlockVolume terrain, MutableBiomeArea biome, ChunkWrapper wrap) {
        super(null, new Location(null, 0, 0, 0), new Location(null, 15, 255, 15));
        this.bx = wrap.x << 4;
        this.bz = wrap.z << 4;
        this.terrain = terrain;
        this.biome = biome;
        this.world = wrap.world;
    }

    @Override
    public void fillBiome(String biomeName) {
        if (this.biome == null) {
            return;
        }
        BiomeType biome = SpongeUtil.getBiome(biomeName.toUpperCase());
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                this.biome.setBiome(this.bx + x, this.bz + z, biome);
            }
        }
    }

    @Override
    public boolean setBiome(int x, int z, String biomeName) {
        modified = true;
        BiomeType biome = SpongeUtil.getBiome(biomeName.toUpperCase());
        this.biome.setBiome(this.bx + x, this.bz + z, biome);
        return true;
    }

    @Override
    public boolean setBlock(int x, int y, int z, int id, int data) {
        modified = true;
        this.terrain.setBlock(this.bx + x, y, this.bz + z, SpongeUtil.getBlockState(id, data), SpongeUtil.CAUSE);
        return true;
    }

    @Override
    public PlotBlock getBlock(int x, int y, int z) {
        return SpongeUtil.getPlotBlock(this.terrain.getBlock(this.bx + x, y, this.bz + z));
    }

    @Override
    public String getWorld() {
        return this.world;
    }

    @Override
    public Location getMax() {
        return new Location(getWorld(), 15 + bx, 255, 15 + bz);
    }

    @Override
    public Location getMin() {
        return new Location(getWorld(), bx, 0, bz);
    }



    public GenChunk clone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    public GenChunk shallowClone() {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
