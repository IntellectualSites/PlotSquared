package com.plotsquared.core.queue;

import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;

public class OffsetLocalBlockQueue extends DelegateLocalBlockQueue {
    private final int ox;
    private final int oy;
    private final int oz;

    public OffsetLocalBlockQueue(LocalBlockQueue parent, int ox, int oy, int oz) {
        super(parent);
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
    }

    @Override public boolean setBiome(int x, int y, BiomeType biome) {
        return super.setBiome(ox + x, oy + y, biome);
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        return super.setBlock(ox + x, oy + y, oz + z, id);
    }
}
