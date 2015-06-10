package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class EconHandler {
    public static EconHandler manager;
    
    public abstract double getMoney(PlotPlayer player);
    public abstract void withdrawMoney(PlotPlayer player, double amount);
    public abstract void depositMoney(PlotPlayer player, double amount);
    public abstract void depositMoney(OfflinePlotPlayer player, double amount);
    public abstract void setPermission(PlotPlayer player, String perm, boolean value);
}
