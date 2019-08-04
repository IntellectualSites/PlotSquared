package com.github.intellectualsites.plotsquared.plot.object;

import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import lombok.Getter;
import lombok.Setter;

public class LegacyPlotBlock extends PlotBlock {

    public static final PlotBlock EVERYTHING = new LegacyPlotBlock((short) 0, (byte) 0);
    public static final PlotBlock[] CACHE = new PlotBlock[65535];

    static {
        for (int i = 0; i < 65535; i++) {
            short id = (short) (i >> 4);
            byte data = (byte) (i & 15);
            CACHE[i] = new LegacyPlotBlock(id, data);
        }
    }

    @Setter private BaseBlock baseBlock = null;
    @Getter public final short id;
    @Getter public final byte data;

    public LegacyPlotBlock(short id, byte data) {
        this.id = id;
        this.data = data;
    }

    @Override public Object getRawId() {
        return this.id;
    }

    @Override public BaseBlock getBaseBlock() {
        if (baseBlock == null) {
            baseBlock = LegacyMapper.getInstance().getBlockFromLegacy(id, data).toBaseBlock();
        }
        return baseBlock;
    }

    @Override public boolean isAir() {
        return this.id == 0;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LegacyPlotBlock other = (LegacyPlotBlock) obj;
        return (this.id == other.id) && ((this.data == other.data) || (this.data == -1) || (
            other.data == -1));
    }

    @Override public int hashCode() {
        return this.id;
    }

    @Override public String toString() {
        if (this.data == -1) {
            return this.id + "";
        }
        return this.id + ":" + this.data;
    }

}
