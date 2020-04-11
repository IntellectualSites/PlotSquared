package com.plotsquared.util;

import com.plotsquared.PlotSquared;
import com.plotsquared.player.ConsolePlayer;
import com.plotsquared.player.OfflinePlotPlayer;
import com.plotsquared.player.PlotPlayer;

public abstract class EconHandler {

    public static EconHandler manager;
    private static boolean initialized;

    public static EconHandler getEconHandler() {
        if (initialized) {
            return manager;
        }
        initialized = true;
        return manager = PlotSquared.get().IMP.getEconomyHandler();
    }

    public double getMoney(PlotPlayer player) {
        if (player instanceof ConsolePlayer) {
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
