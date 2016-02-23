package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class EconHandler {
    public static EconHandler manager;
    
    public double getMoney(final PlotPlayer player) {
        if (ConsolePlayer.isConsole(player)) {
            return Double.MAX_VALUE;
        }
        return getBalance(player);
    }
    
    public abstract double getBalance(PlotPlayer player);

    public abstract void withdrawMoney(final PlotPlayer player, final double amount);
    
    public abstract void depositMoney(final PlotPlayer player, final double amount);
    
    public abstract void depositMoney(final OfflinePlotPlayer player, final double amount);
    
    public void setPermission(final String player, final String perm, final boolean value) {
        setPermission(null, player, perm, value);
    }
    
    public abstract void setPermission(final String world, final String player, final String perm, final boolean value);
    
    public abstract boolean hasPermission(final String world, final String player, final String perm);
    
    public boolean hasPermission(final String player, final String perm) {
        return hasPermission(null, player, perm);
    }
}
