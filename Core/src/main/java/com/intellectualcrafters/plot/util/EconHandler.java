package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class EconHandler {

    public static EconHandler manager;

    public double getMoney(PlotPlayer player) {
        if (ConsolePlayer.isConsole(player)) {
            return Double.MAX_VALUE;
        }
        return getBalance(player);
    }

    public abstract double getBalance(PlotPlayer player);

    public abstract void withdrawMoney(PlotPlayer player, double amount);

    public abstract void depositMoney(PlotPlayer player, double amount);

    public abstract void depositMoney(OfflinePlotPlayer player, double amount);

    public abstract boolean hasPermission(String world, String player, String perm);

    public boolean hasPermission(String player, String perm) {
        return hasPermission(null, player, perm);
    }
}
