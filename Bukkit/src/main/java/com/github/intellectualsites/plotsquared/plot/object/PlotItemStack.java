package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import lombok.Getter;

public class PlotItemStack {

    public final int amount;
    public final String name;
    public final String[] lore;
    @Getter private final PlotBlock plotBlock;

    /**
     * @param id     Legacy numerical item ID
     * @param data   Legacy numerical item data
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     * @deprecated Use {@link PlotItemStack(String, int, String, String...)}
     */
    @Deprecated public PlotItemStack(final int id, final short data, final int amount,
        final String name, final String... lore) {
        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.plotBlock = PlotBlock.get(id, data);
    }

    /**
     * @param id     String ID
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     */
    public PlotItemStack(final String id, final int amount, final String name,
        final String... lore) {
        StringComparison<PlotBlock>.ComparisonResult match = WorldUtil.IMP.getClosestBlock(id);
        this.plotBlock = match.best;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }
}
