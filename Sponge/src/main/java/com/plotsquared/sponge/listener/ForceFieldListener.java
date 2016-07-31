package com.plotsquared.sponge.listener;

import com.flowpowered.math.vector.Vector3d;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.sponge.object.SpongePlayer;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ForceFieldListener {

    private static Set<PlotPlayer> getNearbyPlayers(Player player, Plot plot) {
        Set<PlotPlayer> players = new HashSet<>();
        for (Entity nearbyEntity : player.getNearbyEntities(entity -> entity.getType().equals(EntityTypes.PLAYER))) {
            Player nearbyPlayer = (Player) nearbyEntity;
            PlotPlayer plotPlayer;
            if ((plotPlayer = SpongeUtil.getPlayer(nearbyPlayer)) == null || !plot.equals(plotPlayer.getCurrentPlot())) {
                continue;
            }
            if (!plot.isAdded(plotPlayer.getUUID())) {
                players.add(plotPlayer);
            }

        }
        return players;
    }

    private static PlotPlayer hasNearbyPermitted(Player player, Plot plot) {
        for (Entity nearbyEntity : player.getNearbyEntities(entity -> entity.getType().equals(EntityTypes.PLAYER))) {
            Player nearbyPlayer = (Player) nearbyEntity;
            PlotPlayer plotPlayer;
            if ((plotPlayer = SpongeUtil.getPlayer(nearbyPlayer)) == null || !plot.equals(plotPlayer.getCurrentPlot())) {
                continue;
            }
            if (plot.isAdded(plotPlayer.getUUID())) {
                return plotPlayer;
            }
        }
        return null;
    }

    private static Vector3d calculateVelocity(PlotPlayer player, PlotPlayer e) {
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
        return new Vector3d(x, y, z);
    }

    public static void handleForcefield(Player player, PlotPlayer plotPlayer, Plot plot) {
        if (Flags.FORCEFIELD.isTrue(plot)) {
            UUID uuid = plotPlayer.getUUID();
            if (plot.isAdded(uuid)) {
                Set<PlotPlayer> players = getNearbyPlayers(player, plot);
                for (PlotPlayer oPlayer : players) {
                    ((SpongePlayer) oPlayer).player.setVelocity(calculateVelocity(plotPlayer, oPlayer));
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
