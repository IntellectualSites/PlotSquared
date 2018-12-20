package com.plotsquared.nukkit.util.block;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.block.BasicLocalBlockQueue;
import com.plotsquared.nukkit.NukkitMain;

public class NukkitLocalQueue<T> extends BasicLocalBlockQueue<T> {

    private final Level level;

    public NukkitLocalQueue(String world) {
        super(world);
        this.level = ((NukkitMain) PS.get().IMP).getServer().getLevelByName(world);
    }

    @Override
    public LocalChunk<T> getLocalChunk(int x, int z) {
        return (LocalChunk<T>) new BasicLocalChunk(this, x, z) {
            // Custom stuff?
        };
    }

    @Override
    public void optimize() {

    }

    @Override
    public PlotBlock getBlock(int x, int y, int z) {
        Block block = level.getBlock(getMut(x, y, z));
        if (block == null) {
            return PlotBlock.get(0, 0);
        }
        int id = block.getId();
        if (id == 0) {
            return PlotBlock.get(0, 0);
        }
        return PlotBlock.get(id, block.getDamage());
    }

    @Override
    public void refreshChunk(int x, int z) {

    }

    @Override
    public void fixChunkLighting(int x, int z) {
        // Do nothing
    }

    @Override
    public final void regenChunk(int x, int z) {
        level.regenerateChunk(x, z);
    }

    @Override
    public final void setComponents(LocalChunk<T> lc) {
        setBlocks(lc);
        setBiomes(lc);
    }

    public BaseFullChunk getChunk(int x, int z) {
        return level.getChunk(x, z);
    }

    private Vector3 mutable;
    private Vector3 getMut(int x, int y, int z) {
        mutable.x = x;
        mutable.y = y;
        mutable.z = z;
        return mutable;
    }

    public void setBlocks(LocalChunk<T> lc) {
        FullChunk chunk = level.getChunk(lc.getX(), lc.getZ(), true);
        for (int layer = 0; layer < lc.blocks.length; layer++) {
            PlotBlock[] blocksLayer = (PlotBlock[]) lc.blocks[layer];
            if (blocksLayer != null) {
                int by = layer << 4;
                int j = 0;
                for (int y = by; y < by + 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++, j++) {
                            PlotBlock block = blocksLayer[j];
                            if (block != null) {
                                chunk.setBlock(x, y, z, (int) block.id, (int) block.data);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setBiomes(LocalChunk<T> lc) {
        if (lc.biomes != null) {
            int bx = lc.getX() << 4;
            int bz = lc.getX() << 4;
            String last = null;
            int biome = -1;
            for (int x = 0; x < lc.biomes.length; x++) {
                String[] biomes2 = lc.biomes[x];
                if (biomes2 != null) {
                    for (int y = 0; y < biomes2.length; y++) {
                        String biomeStr = biomes2[y];
                        if (biomeStr != null) {
                            biome = EnumBiome.getBiome(biomeStr.toUpperCase()).getId();
                            level.setBiomeId(bx + x, bz + y, (byte) biome);
                        }
                    }
                }
            }
        }
    }
}