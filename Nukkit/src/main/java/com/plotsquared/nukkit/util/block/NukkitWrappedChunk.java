package com.plotsquared.nukkit.util.block;

import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.block.ScopedLocalBlockQueue;

public class NukkitWrappedChunk extends ScopedLocalBlockQueue {
    private final String world;
    private BaseFullChunk chunk;

    public NukkitWrappedChunk(String world, BaseFullChunk chunk) {
        super(null, new Location(null, 0, 0, 0), new Location(null, 15, 127, 15));
        this.world = world;
        init(chunk);
    }

    public void init(BaseFullChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean setBlock(int x, int y, int z, int id, int data) {
        chunk.setBlock(x, y, z, id, data);
        return true;
    }

    @Override
    public PlotBlock getBlock(int x, int y, int z) {
        int id = chunk.getBlockId(x, y, z);
        if (id == 0) {
            return PlotBlock.get(0, 0);
        }
        int data = chunk.getBlockData(x, y, z);
        return PlotBlock.get(id, data);
    }

    @Override
    public boolean setBiome(int x, int z, String biome) {
        Biome b = EnumBiome.getBiome(biome);
        int id = b.getId();
        chunk.setBiomeId(x, z, id);
        return true;
    }

    @Override
    public void fillBiome(String biome) {
        Biome b = EnumBiome.getBiome(biome);
        int id = b.getId();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBiomeId(x, z, id);
            }
        }
    }

    @Override
    public String getWorld() {
        return world;
    }

    public int getX() {
        return chunk.getX();
    }

    public int getZ() {
        return chunk.getZ();
    }

    @Override
    public Location getMax() {
        return new Location(getWorld(), 15 + (getX() << 4), 255, 15 + (getZ() << 4));
    }

    @Override
    public Location getMin() {
        return new Location(getWorld(), getX() << 4, 0, getZ() << 4);
    }

    public NukkitWrappedChunk clone() {
        return new NukkitWrappedChunk(world, chunk);
    }

    public NukkitWrappedChunk shallowClone() {
        return new NukkitWrappedChunk(world, chunk);
    }
}
