package com.github.intellectualsites.plotsquared.plot.object;

import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StringPlotBlock extends PlotBlock {

    public static final PlotBlock EVERYTHING = new StringPlotBlock("");
    private static final Map<String, StringPlotBlock> STRING_PLOT_BLOCK_CACHE = new HashMap<>();
    @Getter private final String nameSpace;
    @Getter private final String itemId;
    @Getter @Setter private BaseBlock baseBlock = null;
    private boolean isForeign = false;

    public StringPlotBlock(@NonNull final String nameSpace, @NonNull final String itemId) {
        this.nameSpace = nameSpace.toLowerCase(Locale.ENGLISH);
        this.itemId = itemId.toLowerCase(Locale.ENGLISH);
        this.determineForeign();
    }

    public StringPlotBlock(@NonNull final String itemId) {
        if (itemId.contains(":")) {
            final String[] parts = itemId.split(":");
            if (parts.length < 2) {
                throw new IllegalArgumentException(String.format("Cannot parse \"%s\"", itemId));
            }
            this.nameSpace = parts[0].toLowerCase(Locale.ENGLISH);
            this.itemId = parts[1].toLowerCase(Locale.ENGLISH);
        } else {
            this.nameSpace = "minecraft";
            if (itemId.isEmpty()) {
                this.itemId = "air";
            } else {
                this.itemId = itemId.toLowerCase(Locale.ENGLISH);
            }
        }
        this.determineForeign();
    }

    public static StringPlotBlock getOrAdd(@NonNull final String itemId) {
        // final String id = itemId.toLowerCase(Locale.ENGLISH);

        StringPlotBlock plotBlock = STRING_PLOT_BLOCK_CACHE.get(itemId);
        if (plotBlock == null) {
            plotBlock = new StringPlotBlock(itemId);
            STRING_PLOT_BLOCK_CACHE.put(itemId, plotBlock);
        }

        return plotBlock;
    }

    private void determineForeign() {
        this.isForeign = !this.nameSpace.equals("minecraft");
    }

    @Override public String toString() {
        return this.isForeign ? String.format("%s:%s", nameSpace, itemId) : itemId;
    }

    @Override public boolean isAir() {
        return this.itemId.isEmpty() || this.itemId.equalsIgnoreCase("air");
    }

    @Override public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean idEquals(@NonNull final String id) {
        return id.equalsIgnoreCase(this.itemId);
    }

    @Override public Object getRawId() {
        return this.getItemId();
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
        StringPlotBlock other = (StringPlotBlock) obj;
        return other.nameSpace.equals(this.nameSpace) && other.itemId.equals(this.itemId);
    }
}
