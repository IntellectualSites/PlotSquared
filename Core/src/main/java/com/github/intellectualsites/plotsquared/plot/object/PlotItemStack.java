package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import lombok.Getter;

public class PlotItemStack {
    // public final int id;
    // public final short data;
    @Getter
    private final PlotBlock plotBlock;
    public final int amount;
    public final String name;
    public final String[] lore;

    @Deprecated
    public PlotItemStack(final int id, final short data, final int amount, final String name,
        final String... lore) {
        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.plotBlock = PlotBlock.get(id, data);
    }

    public PlotItemStack(final String id, final int amount, final String name,
        final String... lore) {
        StringComparison<PlotBlock>.ComparisonResult match = WorldUtil.IMP.getClosestBlock(id);
        this.plotBlock = match.best;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }
}
