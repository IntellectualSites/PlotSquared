package com.plotsquared.core.queue;

import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.Getter;

import java.util.HashMap;

public class LocalChunk {
    @Getter private final BasicQueueCoordinator parent;
    @Getter private final int z;
    @Getter private final int x;

    @Getter private final BaseBlock[][] baseblocks;
    @Getter private final BiomeType[][] biomes;
    @Getter private final HashMap<BlockVector3, CompoundTag> tiles = new HashMap<>();

    public LocalChunk(BasicQueueCoordinator parent, int x, int z) {
        this.parent = parent;
        this.x = x;
        this.z = z;
        baseblocks = new BaseBlock[16][];
        biomes = new BiomeType[16][];
    }

    public void setBiome(final int x, final int y, final int z, final BiomeType biomeType) {
        final int i = MainUtil.CACHE_I[y][x][z];
        final int j = MainUtil.CACHE_J[y][x][z];
        BiomeType[] array = this.biomes[i];
        if (array == null) {
            array = this.biomes[i] = new BiomeType[4096];
        }
        array[j] = biomeType;
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
        tiles.put(BlockVector3.at(x, y, z), tag);
    }
}
