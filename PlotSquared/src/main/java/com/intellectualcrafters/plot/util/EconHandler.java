package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class EconHandler {
    // TODO economy shit
    public static double getBalance(final PlotPlayer player) {
        return PlotSquared.economy.getBalance(player.getName());
    }

    public static void withdrawPlayer(final PlotPlayer player, final double amount) {
        PlotSquared.economy.withdrawPlayer(player.getName(), amount);
    }

    public static void depositPlayer(final PlotPlayer player, final double amount) {
        PlotSquared.economy.depositPlayer(player.getName(), amount);
    }

    public static void depositPlayer(final OfflinePlotPlayer player, final double amount) {
        PlotSquared.economy.depositPlayer(player.getName(), amount);
    }
}
