package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.util.InventoryUtil;

public class PlotInventory {

    public final PlotPlayer player;
    public final int size;
    private String title;
    private final PlotItemStack[] items;
    
    private boolean open = false;
    
    public PlotInventory(PlotPlayer player) {
        this.size = 4;
        this.title = null;
        this.player = player;
        items = InventoryUtil.manager.getItems(player);
    }

    public PlotInventory(PlotPlayer player, int size, String name) {
        this.size = size;
        this.title = name == null ? "" : name;
        this.player = player;
        items = new PlotItemStack[size * 9];
    }
    
    public boolean onClick(int index) {
        return true;
    }
    
    public void openInventory() {
        if (title == null) {
            return;
        }
        open = true;
        InventoryUtil.manager.open(this);
    }
    
    public void close() {
        if (title == null) {
            return;
        }
        InventoryUtil.manager.close(this);
        open = false;
    }
    
    public void setItem(int index, PlotItemStack item) {
        items[index] = item;
        InventoryUtil.manager.setItem(this, index, item);
    }
    
    public PlotItemStack getItem(int index) {
        if (index < 0 || index >= items.length) {
            return null;
        }
        return items[index];
    }
    
    public void setTitle(String title) {
        if (title == null) {
            return;
        }
        boolean tmp = open;
        close();
        this.title = title;
        if (tmp) {
            openInventory();
        }
    }
    
    public PlotItemStack[] getItems() {
        return items;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public boolean isOpen() {
        return open;
    }
    
}
