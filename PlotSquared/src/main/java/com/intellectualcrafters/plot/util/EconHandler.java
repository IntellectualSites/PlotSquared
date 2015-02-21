package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class EconHandler {
    // TODO economy shit
    public static double getBalance(PlotPlayer player) {
        return PlotSquared.economy.getBalance(player.getName());
    }
    
    public static void withdrawPlayer(PlotPlayer player, double amount) {
        EconHandler.withdrawPlayer(player.getName(), amount);
    }
}
