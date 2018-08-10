package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;

public class PlotItemStack {
    public final int id;
    public final short data;
    public final int amount;
    public final String name;
    public final String[] lore;

    @Deprecated
    public PlotItemStack(final int id, final short data, final int amount, final String name,
        final String... lore) {
        this.id = id;
        this.data = data;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public PlotItemStack(final String id, final int amount, final String name,
        final String... lore) {
        StringComparison<PlotBlock>.ComparisonResult match = WorldUtil.IMP.getClosestBlock(id);
        final PlotBlock block = match.best;
        this.id = block.id;
        data = block.data;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }
}
