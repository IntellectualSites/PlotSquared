package com.plotsquared.core.util;

import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.player.PlotPlayer;

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
