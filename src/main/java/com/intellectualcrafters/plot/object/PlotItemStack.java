package com.intellectualcrafters.plot.object;

public class PlotItemStack {
    public final int id;
    public final short data;
    public final int amount;
    public final String name;
    public final String[] lore;
    
    public PlotItemStack(int id, short data, int amount, String name, String... lore) {
        this.id = id;
        this.data = data;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }
}
