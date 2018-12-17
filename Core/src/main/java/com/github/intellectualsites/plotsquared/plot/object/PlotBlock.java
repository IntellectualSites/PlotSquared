package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.config.Settings;
import lombok.NonNull;

import java.util.Collection;

public abstract class PlotBlock {

    public static boolean isEverything(@NonNull final PlotBlock block) {
        return block.equals(LegacyPlotBlock.EVERYTHING) || block.equals(StringPlotBlock.EVERYTHING);
    }

    public static boolean containsEverything(@NonNull final Collection<PlotBlock> blocks) {
        for (final PlotBlock block : blocks) {
            if (isEverything(block)) {
                return true;
            }
        }
        return false;
    }

    protected PlotBlock() {
    }

    public static PlotBlock get(char combinedId) {
        switch (combinedId) {
            case 0:
                return null;
            case 1:
                return get(0, 0);
            default:
                return get(combinedId >> 4, combinedId & 15);
        }
    }

    public abstract boolean isAir();

    public static StringPlotBlock get(@NonNull final String itemId) {
        if (Settings.Enabled_Components.BLOCK_CACHE) {
            return StringPlotBlock.getOrAdd(itemId);
        }
        return new StringPlotBlock(itemId);
    }

    public static PlotBlock get(int id, int data) {
        return Settings.Enabled_Components.BLOCK_CACHE && data > 0 ?
            LegacyPlotBlock.CACHE[(id << 4) + data] :
            new LegacyPlotBlock((short) id, (byte) data);
    }

    public static PlotBlock getEmptyData(@NonNull final PlotBlock plotBlock) {
        if (plotBlock instanceof StringPlotBlock) {
            return plotBlock;
        }
        return get(((LegacyPlotBlock) plotBlock).getId(), (byte) 0);
    }

    public final boolean equalsAny(final int id, @NonNull final String stringId) {
        if (this instanceof StringPlotBlock) {
            final StringPlotBlock stringPlotBlock = (StringPlotBlock) this;
            return stringPlotBlock.idEquals(stringId);
        }
        final LegacyPlotBlock legacyPlotBlock = (LegacyPlotBlock) this;
        return legacyPlotBlock.id == id;
    }

    @Override public abstract boolean equals(Object obj);

    @Override public abstract int hashCode();

    @Override public abstract String toString();

    public abstract Object getRawId();

}
