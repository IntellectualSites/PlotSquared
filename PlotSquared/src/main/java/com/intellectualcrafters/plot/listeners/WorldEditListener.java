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

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Citymonstret
 */
public class WorldEditListener implements Listener {

    public final Set<String> blockedcmds = new HashSet<String>(Arrays.asList("/gmask", "//gmask", "/worldedit:gmask"));
    public final Set<String> restrictedcmds = new HashSet<String>(Arrays.asList("/up", "//up", "/worldedit:up"));

    private boolean isPlotWorld(final Location l) {
        return (PlotMain.isPlotWorld(l.getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDelete(final PlotDeleteEvent e) {
        final String world = e.getWorld();
        final PlotId id = e.getPlotId();
        final Plot plot = PlotMain.getPlots(world).get(id);
        if ((plot == null) || (plot.owner == null)) {
            return;
        }
        final Player player = Bukkit.getPlayer(plot.owner);
        if (player == null) {
            return;
        }
        if (!world.equals(player.getWorld().getName())) {
            return;
        }
        if (PlotMain.hasPermission(player, "plots.worldedit.bypass")) {
            return;
        }
        PWE.setNoMask(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent e) {
        final Block b = e.getClickedBlock();
        if (b == null) {
            return;
        }
        final Player p = e.getPlayer();
        final Location l = b.getLocation();
        if (!isPlotWorld(l)) {
            return;
        }
        p.getItemInHand();
        if ((p.getItemInHand() == null) || (p.getItemInHand().getType() == Material.AIR)) {
            return;
        }
        final Plot plot = PlotHelper.getCurrentPlot(b.getLocation());
        if (plot != null) {
            if ((plot != null) && plot.hasOwner() && (plot.helpers != null) && (plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId()))) {
                PWE.setMask(p, l);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        if (PlotMain.hasPermission(p, "plots.worldedit.bypass") || !PlotMain.isPlotWorld(p.getWorld())) {
            return;
        }
        String cmd = e.getMessage().toLowerCase();

        if (cmd.contains(" ")) {
            cmd = cmd.substring(0, cmd.indexOf(" "));
        }
        if (this.restrictedcmds.contains(cmd)) {
            final Plot plot = PlayerFunctions.getCurrentPlot(p);
            if ((plot == null) || !(plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId()))) {
                e.setCancelled(true);
            }
        } else if (this.blockedcmds.contains(cmd)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (PlotMain.hasPermission(p, "plots.worldedit.bypass")) {
            return;
        }
        final Location l = p.getLocation();
        if (isPlotWorld(l)) {
            PWE.setMask(p, l);
        } else {
            PWE.removeMask(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent e) {
        final Location t = e.getTo();
        if (!isPlotWorld(t)) {
            return;
        }
        final Location f = e.getFrom();
        final Player p = e.getPlayer();

        if ((f.getBlockX() != t.getBlockX()) || (f.getBlockZ() != t.getBlockZ())) {
            final PlotId idF = PlayerFunctions.getPlot(f);
            final PlotId idT = PlayerFunctions.getPlot(t);
            if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
                return;
            }
            if ((idT != null) && !(idF == idT)) {
                PWE.setMask(p, t);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(final PlayerPortalEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return;
        }
        final Player p = e.getPlayer();
        final Location t = e.getTo();
        final Location f = e.getFrom();
        if (t == null) {
            PWE.removeMask(p);
            return;
        }
        if (isPlotWorld(t)) {
            PWE.setMask(p, t);
            return;
        }
        if ((f != null) && isPlotWorld(f)) {
            PWE.removeMask(p);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return;
        }
        final Player p = e.getPlayer();
        final Location t = e.getTo();
        final Location f = e.getFrom();
        if (!isPlotWorld(t)) {
            if (isPlotWorld(f)) {
                PWE.removeMask(p);
            } else {
                return;
            }
        }
        PWE.setMask(p, t);
    }
}
