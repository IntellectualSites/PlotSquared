////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Citymonstret on 2014-10-24.
 */
public class ForceFieldListener implements Listener {

    public ForceFieldListener(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private Set<Player> getNearbyPlayers(final Player player, final Plot plot) {
        final Set<Player> players = new HashSet<>();
        Player oPlayer = null;
        for (final Entity entity : player.getNearbyEntities(5d, 5d, 5d)) {
            if (!(entity instanceof Player) || ((oPlayer = (Player) entity) == null) || !PlayerFunctions.isInPlot(oPlayer) || !PlayerFunctions.getCurrentPlot(oPlayer).equals(plot)) {
                continue;
            }
            if (!plot.hasRights(oPlayer)) {
                players.add(oPlayer);
            }
        }
        return players;
    }

    private Player hasNearbyPermitted(final Player player, final Plot plot) {
        Player oPlayer = null;
        for (final Entity entity : player.getNearbyEntities(5d, 5d, 5d)) {
            if (!(entity instanceof Player) || ((oPlayer = (Player) entity) == null) || !PlayerFunctions.isInPlot(oPlayer) || !PlayerFunctions.getCurrentPlot(oPlayer).equals(plot)) {
                continue;
            }
            if (plot.hasRights(oPlayer)) {
                return oPlayer;
            }
        }
        return null;
    }

    public Vector calculateVelocity(final Player p, final Player e) {
        final Location playerLocation = p.getLocation();
        final Location oPlayerLocation = e.getLocation();
        final double playerX = playerLocation.getX(), playerY = playerLocation.getY(), playerZ = playerLocation.getZ(), oPlayerX = oPlayerLocation.getX(), oPlayerY = oPlayerLocation.getY(), oPlayerZ = oPlayerLocation.getZ();
        double x = 0d, y = 0d, z = 0d;
        if (playerX < oPlayerX) {
            x = 1.0d;
        } else if (playerX > oPlayerX) {
            x = -1.0d;
        }
        if (playerY < oPlayerY) {
            y = 0.5d;
        } else if (playerY > oPlayerY) {
            y = -0.5d;
        }
        if (playerZ < oPlayerZ) {
            z = 1.0d;
        } else if (playerZ > oPlayerZ) {
            z = -1.0d;
        }
        return new Vector(x, y, z);
    }

    @EventHandler
    public void onPlotEntry(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (!PlayerFunctions.isInPlot(player)) {
            return;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(player);
        if ((plot.settings.getFlag("forcefield") != null) && plot.settings.getFlag("forcefield").getValue().equals("true")) {
            if (!PlotListener.booleanFlag(plot, "forcefield")) {
                if (plot.hasRights(player)) {
                    final Set<Player> players = getNearbyPlayers(player, plot);
                    for (final Player oPlayer : players) {
                        oPlayer.setVelocity(calculateVelocity(player, oPlayer));
                    }
                } else {
                    final Player oPlayer = hasNearbyPermitted(player, plot);
                    if (oPlayer == null) {
                        return;
                    }
                    player.setVelocity(calculateVelocity(oPlayer, player));
                }
            }
        }
    }
}
