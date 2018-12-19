package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.configuration.serialization.ConfigurationSerializable;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;

public abstract class PlotBlock implements ConfigurationSerializable {

    private static Class<?> conversionType;
    private static BlockRegistry blockRegistry;

    protected PlotBlock() {
    }

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

    public static PlotBlock deserialize(@NonNull final Map<String, Object> map) {
        if (map.containsKey("material")) {
            final Object object = map.get("material");
            return get(object.toString());
        }
        return null;
    }

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

    public static PlotBlock get(@NonNull final BaseBlock baseBlock) {
        StringPlotBlock plotBlock = get(baseBlock.getBlockType().getId());
        plotBlock.setBaseBlock(baseBlock);
        return plotBlock;
    }

    public static PlotBlock get(@NonNull final Object type) {
        if (blockRegistry == null) {
            blockRegistry = PlotSquared.imp().getBlockRegistry();
            if (blockRegistry == null) {
                throw new UnsupportedOperationException(
                    "The PlotSquared implementation has not registered a custom block registry."
                        + " This method can't be used.");
            }
            conversionType = blockRegistry.getType();
        }
        if (!type.getClass().equals(conversionType)) {
            throw new UnsupportedOperationException(
                "The PlotSquared implementation has not registered a block registry for this object type");
        }
        return blockRegistry.getPlotBlock(type);
    }

    @Override public Map<String, Object> serialize() {
        return ImmutableMap.of("material", this.getRawId());
    }

    public <T> T to(@NonNull final Class<T> clazz) {
        if (blockRegistry == null) {
            blockRegistry = PlotSquared.imp().getBlockRegistry();
            if (blockRegistry == null) {
                throw new UnsupportedOperationException(
                    "The PlotSquared implementation has not registered a custom block registry."
                        + " This method can't be used.");
            }
            conversionType = blockRegistry.getType();
        }
        if (!clazz.equals(conversionType)) {
            throw new UnsupportedOperationException(
                "The PlotSquared implementation has not registered a block registry for this object type");
        }
        return clazz.cast(blockRegistry.getItem(this));
    }

    public abstract boolean isAir();

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
