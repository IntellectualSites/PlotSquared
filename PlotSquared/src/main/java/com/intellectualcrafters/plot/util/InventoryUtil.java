package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;

/**
 * This class is only used by internal functions, for most cases use the PlotInventory class
 */
public abstract class InventoryUtil {
    
    /**
     * This class is only used by internal functions, for most cases use the PlotInventory class
     */
    public static InventoryUtil manager = null;
    public abstract void open(PlotInventory inv);
    public abstract void close(PlotInventory inv);
    public abstract void setItem(PlotInventory plotInventory, int index, PlotItemStack item);
    public abstract PlotItemStack[] getItems(PlotPlayer player);
    public abstract boolean isOpen(PlotInventory plotInventory);
}
