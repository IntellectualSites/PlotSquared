package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class EconHandler {
    public static EconHandler manager;
    
    public abstract double getMoney(PlotPlayer player);
    public abstract double withdrawMoney(PlotPlayer player, double amount);
    public abstract double depositMoney(PlotPlayer player, double amount);
    public abstract double depositMoney(OfflinePlotPlayer player, double amount);
    public abstract void setPermission(PlotPlayer player, String perm, boolean value);
    public abstract boolean getPermission(PlotPlayer player, String perm);
}
