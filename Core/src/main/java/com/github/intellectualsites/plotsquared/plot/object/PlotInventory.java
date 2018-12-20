package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.InventoryUtil;

public class PlotInventory {

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

    public boolean onClick(int index) {
        return true;
    }

    public void openInventory() {
        if (this.title == null) {
            return;
        }
        this.open = true;
        InventoryUtil.manager.open(this);
    }

    public void close() {
        if (this.title == null) {
            return;
        }
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
