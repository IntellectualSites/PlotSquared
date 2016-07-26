package com.plotsquared.bukkit.listeners;

import com.google.common.collect.Iterables;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ForceFieldListener {

    private static Set<PlotPlayer> getNearbyPlayers(Player player, Plot plot) {
        Set<PlotPlayer> players = new HashSet<>();
        for (Player nearPlayer : Iterables.filter(player.getNearbyEntities(5d, 5d, 5d),Player.class)) {
            PlotPlayer plotPlayer;
            if ((plotPlayer = BukkitUtil.getPlayer(nearPlayer)) == null || !plot.equals(plotPlayer.getCurrentPlot())) {
                continue;
            }
            if (!plot.isAdded(plotPlayer.getUUID())) {
                players.add(plotPlayer);
            }
        }
        return players;
    }

    private static PlotPlayer hasNearbyPermitted(Player player, Plot plot) {
        for (Player nearPlayer : Iterables.filter(player.getNearbyEntities(5d, 5d, 5d),Player.class)) {
            PlotPlayer plotPlayer;
            if ((plotPlayer = BukkitUtil.getPlayer(nearPlayer)) == null || !plot.equals(plotPlayer.getCurrentPlot())) {
                continue;
            }
            if (plot.isAdded(plotPlayer.getUUID())) {
                return plotPlayer;
            }
        }
        return null;
    }

    private static Vector calculateVelocity(PlotPlayer player, PlotPlayer e) {
        Location playerLocation = player.getLocationFull();
        Location oPlayerLocation = e.getLocation();
        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();
        double playerZ = playerLocation.getZ();
        double oPlayerX = oPlayerLocation.getX();
        double oPlayerY = oPlayerLocation.getY();
        double oPlayerZ = oPlayerLocation.getZ();
        double x = 0d;
        if (playerX < oPlayerX) {
            x = 1.0d;
        } else if (playerX > oPlayerX) {
            x = -1.0d;
        }
        double y = 0d;
        if (playerY < oPlayerY) {
            y = 0.5d;
        } else if (playerY > oPlayerY) {
            y = -0.5d;
        }
        double z = 0d;
        if (playerZ < oPlayerZ) {
            z = 1.0d;
        } else if (playerZ > oPlayerZ) {
            z = -1.0d;
        }
        return new Vector(x, y, z);
    }

    public static void handleForcefield(Player player, PlotPlayer plotPlayer, Plot plot) {
        if (Flags.FORCEFIELD.isTrue(plot)) {
            UUID uuid = plotPlayer.getUUID();
            if (plot.isAdded(uuid)) {
                Set<PlotPlayer> players = getNearbyPlayers(player, plot);
                for (PlotPlayer oPlayer : players) {
                    ((BukkitPlayer) oPlayer).player.setVelocity(calculateVelocity(plotPlayer, oPlayer));
                }
            } else {
                PlotPlayer oPlayer = hasNearbyPermitted(player, plot);
                if (oPlayer == null) {
                    return;
                }
                player.setVelocity(calculateVelocity(oPlayer, plotPlayer));
            }
        }
    }
}
