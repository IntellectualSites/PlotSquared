package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.google.common.collect.Iterables;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused") public class ForceFieldListener {

    private static Set<PlotPlayer> getNearbyPlayers(Player player, Plot plot) {
        Set<PlotPlayer> players = new HashSet<>();
        for (Player nearPlayer : Iterables
            .filter(player.getNearbyEntities(5d, 5d, 5d), Player.class)) {
            PlotPlayer plotPlayer;
            if ((plotPlayer = BukkitUtil.getPlayer(nearPlayer)) == null || !plot
                .equals(plotPlayer.getCurrentPlot())) {
                continue;
            }
            if (!plot.isAdded(plotPlayer.getUUID())) {
                players.add(plotPlayer);
            }
        }
        return players;
    }

    private static PlotPlayer hasNearbyPermitted(Player player, Plot plot) {
        for (Player nearPlayer : Iterables
            .filter(player.getNearbyEntities(5d, 5d, 5d), Player.class)) {
            PlotPlayer plotPlayer;
            if ((plotPlayer = BukkitUtil.getPlayer(nearPlayer)) == null || !plot
                .equals(plotPlayer.getCurrentPlot())) {
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
                    if (!Permissions
                        .hasPermission(oPlayer, Captions.PERMISSION_ADMIN_ENTRY_FORCEFIELD)) {
                        ((BukkitPlayer) oPlayer).player
                            .setVelocity(calculateVelocity(plotPlayer, oPlayer));
                    }
                }
            } else {
                PlotPlayer oPlayer = hasNearbyPermitted(player, plot);
                if (oPlayer == null) {
                    return;
                }
                if (!Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_ENTRY_FORCEFIELD)) {
                    player.setVelocity(calculateVelocity(oPlayer, plotPlayer));
                }
            }
        }
    }
}
