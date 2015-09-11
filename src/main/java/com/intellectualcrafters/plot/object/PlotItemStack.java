package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.util.BlockManager;

public class PlotItemStack
{
    public final int id;
    public final short data;
    public final int amount;
    public final String name;
    public final String[] lore;

    @Deprecated
    public PlotItemStack(final int id, final short data, final int amount, final String name, final String... lore)
    {
        this.id = id;
        this.data = data;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public PlotItemStack(final String id, final int amount, final String name, final String... lore)
    {
        final PlotBlock block = BlockManager.manager.getPlotBlockFromString(id);
        this.id = block.id;
        data = block.data;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }
}
