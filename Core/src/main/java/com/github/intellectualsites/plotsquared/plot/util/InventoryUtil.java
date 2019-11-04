package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.PlotInventory;
import com.github.intellectualsites.plotsquared.plot.object.PlotItemStack;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

/**
 * This class is only used by internal functions, for most cases use the PlotInventory class
 */
public abstract class InventoryUtil {

    /**
     * This class is only used by internal functions, for most cases use the PlotInventory class
     */
    public static InventoryUtil manager = null;

    public abstract void open(final PlotInventory inv);

    public abstract void close(final PlotInventory inv);

    public abstract void setItem(final PlotInventory plotInventory, final int index,
        final PlotItemStack item);

    public abstract PlotItemStack[] getItems(final PlotPlayer player);

    public abstract boolean isOpen(final PlotInventory plotInventory);
}
