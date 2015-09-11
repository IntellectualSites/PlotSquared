package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.util.InventoryUtil;

public class PlotInventory
{

    public final PlotPlayer player;
    public final int size;
    private String title;
    private final PlotItemStack[] items;

    private boolean open = false;

    public PlotInventory(final PlotPlayer player)
    {
        size = 4;
        title = null;
        this.player = player;
        items = InventoryUtil.manager.getItems(player);
    }

    public PlotInventory(final PlotPlayer player, final int size, final String name)
    {
        this.size = size;
        title = name == null ? "" : name;
        this.player = player;
        items = new PlotItemStack[size * 9];
    }

    public boolean onClick(final int index)
    {
        return true;
    }

    public void openInventory()
    {
        if (title == null) { return; }
        open = true;
        InventoryUtil.manager.open(this);
    }

    public void close()
    {
        if (title == null) { return; }
        InventoryUtil.manager.close(this);
        open = false;
    }

    public void setItem(final int index, final PlotItemStack item)
    {
        items[index] = item;
        InventoryUtil.manager.setItem(this, index, item);
    }

    public PlotItemStack getItem(final int index)
    {
        if ((index < 0) || (index >= items.length)) { return null; }
        return items[index];
    }

    public void setTitle(final String title)
    {
        if (title == null) { return; }
        final boolean tmp = open;
        close();
        this.title = title;
        if (tmp)
        {
            openInventory();
        }
    }

    public PlotItemStack[] getItems()
    {
        return items;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean isOpen()
    {
        return open;
    }

}
