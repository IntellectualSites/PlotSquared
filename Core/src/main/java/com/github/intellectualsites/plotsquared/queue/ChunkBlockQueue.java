package com.github.intellectualsites.plotsquared.queue;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ChunkBlockQueue extends ScopedLocalBlockQueue {

    public final BiomeType[] biomeGrid;
    public final BlockState[][][] result;
    private final int width;
    private final int length;
    @Deprecated private final int area;
    private final BlockVector3 bot;
    private final BlockVector3 top;

    public ChunkBlockQueue(BlockVector3 bot, BlockVector3 top, boolean biomes) {
        super(null, new Location(null, 0, 0, 0), new Location(null, 15, 255, 15));
        this.width = top.getX() - bot.getX() + 1;
        this.length = top.getZ() - bot.getZ() + 1;
        this.area = width * length;
        this.result = new BlockState[256][][];
        this.biomeGrid = biomes ? new BiomeType[width * length] : null;
        this.bot = bot;
        this.top = top;
    }

    public BlockState[][][] getBlocks() {
        return result;
    }

    @Override public void fillBiome(BiomeType biomeType) {
        if (biomeGrid == null) {
            return;
        }
        Arrays.fill(biomeGrid, biomeType);
    }

    @Override public boolean setBiome(int x, int z, BiomeType biomeType) {
        if (this.biomeGrid != null) {
            biomeGrid[(z * width) + x] = biomeType;
            return true;
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        this.storeCache(x, y, z, id);
        return true;
    }

    private void storeCache(final int x, final int y, final int z, final BlockState id) {
        BlockState[][] resultY = result[y];
        if (resultY == null) {
            result[y] = resultY = new BlockState[length][];
        }
        BlockState[] resultYZ = resultY[z];
        if (resultYZ == null) {
            resultY[z] = resultYZ = new BlockState[width];
        }
        resultYZ[x] = id;
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        this.storeCache(x, y, z, id.toImmutableState());
        return true;
    }

    @Override @Nullable public BlockState getBlock(int x, int y, int z) {
        BlockState[][] blocksY = result[y];
        if (blocksY != null) {
            BlockState[] blocksYZ = blocksY[z];
            if (blocksYZ != null) {
                return blocksYZ[x];
            }
        }
        return null;
    }

    @Override @Nullable public String getWorld() {
        return null;
    }

    @Override public Location getMax() {
        return new Location(getWorld(), top.getX(), top.getY(), top.getZ());
    }

    @Override public Location getMin() {
        return new Location(getWorld(), bot.getX(), bot.getY(), bot.getZ());
    }
}
