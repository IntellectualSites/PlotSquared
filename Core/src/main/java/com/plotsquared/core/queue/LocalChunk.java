package com.plotsquared.core.queue;

import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.HashMap;

public class LocalChunk {
    public final BasicQueueCoordinator parent;
    public final int z;
    public final int x;

    public BaseBlock[][] baseblocks;
    public BiomeType[][] biomes;
    public HashMap<BlockVector3, CompoundTag> tiles = null;

    public LocalChunk(BasicQueueCoordinator parent, int x, int z) {
        this.parent = parent;
        this.x = x;
        this.z = z;
        baseblocks = new BaseBlock[16][];
    }

    /**
     * Get the parent queue this chunk belongs to
     *
     * @return
     */
    public BasicQueueCoordinator getParent() {
        return parent;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public void setBiome(int x, int z, BiomeType biomeType) {
        if (this.biomes == null) {
            this.biomes = new BiomeType[16][];
        }
        BiomeType[] index = this.biomes[x];
        if (index == null) {
            index = this.biomes[x] = new BiomeType[16];
        }
        index[z] = biomeType;
    }

    public long longHash() {
        return MathMan.pairInt(x, z);
    }

    @Override public int hashCode() {
        return MathMan.pair((short) x, (short) z);
    }

    public void setBlock(final int x, final int y, final int z, final BaseBlock baseBlock) {
        final int i = MainUtil.CACHE_I[y][x][z];
        final int j = MainUtil.CACHE_J[y][x][z];
        BaseBlock[] array = baseblocks[i];
        if (array == null) {
            array = (baseblocks[i] = new BaseBlock[4096]);
        }
        array[j] = baseBlock;
    }

    public void setTile(final int x, final int y, final int z, final CompoundTag tag) {
        if (tiles == null) {
            tiles = new HashMap<>();
        }
        tiles.put(BlockVector3.at(x, y, z), tag);
    }
}
