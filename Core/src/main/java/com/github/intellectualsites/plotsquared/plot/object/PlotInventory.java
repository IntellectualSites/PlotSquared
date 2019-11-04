package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.util.InventoryUtil;
import lombok.NonNull;

public class PlotInventory {

    private static final String META_KEY = "inventory";
    public final PlotPlayer player;
    public final int size;
    private final PlotItemStack[] items;
    private String title;
    private boolean open = false;

    public PlotInventory(PlotPlayer player) {
        this.size = 4;
        this.title = null;
        this.player = player;
        this.items = InventoryUtil.manager.getItems(player);
    }

    public PlotInventory(PlotPlayer player, int size, String name) {
        this.size = size;
        this.title = name == null ? "" : name;
        this.player = player;
        this.items = new PlotItemStack[size * 9];
    }

    public static boolean hasPlotInventoryOpen(@NonNull final PlotPlayer plotPlayer) {
        return getOpenPlotInventory(plotPlayer) != null;
    }

    public static PlotInventory getOpenPlotInventory(@NonNull final PlotPlayer plotPlayer) {
        return plotPlayer.getMeta(META_KEY, null);
    }

    public static void setPlotInventoryOpen(@NonNull final PlotPlayer plotPlayer,
        @NonNull final PlotInventory plotInventory) {
        plotPlayer.setMeta(META_KEY, plotInventory);
    }

    public static void removePlotInventoryOpen(@NonNull final PlotPlayer plotPlayer) {
        plotPlayer.deleteMeta(META_KEY);
    }

    public boolean onClick(int index) {
        return true;
    }

    public void openInventory() {
        if (this.title == null) {
            return;
        }
        if (hasPlotInventoryOpen(player)) {
            PlotSquared.debug(String.format("Failed to open plot inventory for %s "
                + "because the player already has an open plot inventory", player.getName()));
        } else {
            this.open = true;
            setPlotInventoryOpen(player, this);
            InventoryUtil.manager.open(this);
        }
    }

    public void close() {
        if (this.title == null) {
            return;
        }
        removePlotInventoryOpen(player);
        InventoryUtil.manager.close(this);
        this.open = false;
    }

    public void setItem(int index, PlotItemStack item) {
        this.items[index] = item;
        InventoryUtil.manager.setItem(this, index, item);
    }

    public PlotItemStack getItem(int index) {
        if ((index < 0) || (index >= this.items.length)) {
            return null;
        }
        return this.items[index];
    }

    public PlotItemStack[] getItems() {
        return this.items;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        boolean tmp = this.open;
        close();
        this.title = title;
        if (tmp) {
            openInventory();
        }
    }

    public boolean isOpen() {
        return this.open;
    }

}
