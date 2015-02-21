package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class EconHandler {
    // TODO economy shit
    public static double getBalance(PlotPlayer player) {
        return PlotSquared.economy.getBalance(player.getName());
    }
    
    public static void withdrawPlayer(PlotPlayer player, double amount) {
        PlotSquared.economy.withdrawPlayer(player.getName(), amount);
    }
    
    public static void depositPlayer(PlotPlayer player, double amount) {
        PlotSquared.economy.depositPlayer(player.getName(), amount);
    }
    
    public static void depositPlayer(OfflinePlotPlayer player, double amount) {
        PlotSquared.economy.depositPlayer(player.getName(), amount);
    }
}
