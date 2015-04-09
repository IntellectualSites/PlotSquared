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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.PWE;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.function.mask.Mask;

/**
 * @author Citymonstret
 * @author Empire92
 */
public class WorldEditListener implements Listener {
    final List<String> monitored = Arrays.asList(new String[] { "set", "replace", "overlay", "walls", "outline", "deform", "hollow", "smooth", "move", "stack", "naturalize", "paste", "count", "regen", "copy", "cut", "" });
    public final Set<String> blockedcmds = new HashSet<>(Arrays.asList("/gmask", "//gmask", "/worldedit:gmask"));
    public final Set<String> restrictedcmds = new HashSet<>(Arrays.asList("/up", "//up", "/worldedit:up"));

    private boolean isPlotWorld(final Location l) {
        return (PlotSquared.isPlotWorld(l.getWorld().getName()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDelete(final PlotDeleteEvent e) {
        final String world = e.getWorld();
        final PlotId id = e.getPlotId();
        final Plot plot = PlotSquared.getPlots(world).get(id);
        if (plot == null) {
            return;
        }
        HashSet<UUID> members = PlotHandler.getOwners(plot);
        if (members == null) {
            return;
        }
        members.addAll(plot.helpers);
        for (UUID member : members) {
            final PlotPlayer player = UUIDHandler.getPlayer(member);
            if (player == null) {
                continue;
            }
            if (!world.equals(player.getLocation().getWorld())) {
                return;
            }
            if (Permissions.hasPermission(player, "plots.worldedit.bypass")) {
                return;
            }
            PWE.setNoMask(player);
        }
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
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        final com.intellectualcrafters.plot.object.Location loc = pp.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (plot.hasOwner() && (plot.helpers != null) && (plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(pp.getUUID()))) {
                PWE.setMask(BukkitUtil.getPlayer(p), loc, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        if (!PlotSquared.isPlotWorld(p.getWorld().getName()) || Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
            return;
        }
        String cmd = e.getMessage().toLowerCase();
        if (cmd.contains(" ")) {
            cmd = cmd.substring(0, cmd.indexOf(" "));
        }
        if (this.restrictedcmds.contains(cmd)) {
            final Plot plot = MainUtil.getPlot(pp.getLocation());
            if ((plot == null) || !(plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(pp.getUUID()))) {
                e.setCancelled(true);
            }
            return;
        } else if (this.blockedcmds.contains(cmd)) {
            e.setCancelled(true);
            return;
        }
        if (!Settings.REQUIRE_SELECTION) {
            return;
        }
        for (final String c : this.monitored) {
            if (cmd.equals("//" + c) || cmd.equals("/" + c) || cmd.equals("/worldedit:/" + c)) {
                final Selection selection = PlotSquared.worldEdit.getSelection(p);
                if (selection == null) {
                    return;
                }
                final BlockVector pos1 = selection.getNativeMinimumPoint().toBlockVector();
                final BlockVector pos2 = selection.getNativeMaximumPoint().toBlockVector();
                final LocalSession session = PlotSquared.worldEdit.getSession(p);
                final Mask mask = session.getMask();
                if (mask == null) {
                    MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "Both points");
                    return;
                }
                if (!mask.test(pos1)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "Position 1");
                }
                if (!mask.test(pos2)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "Position 2");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final Location l = p.getLocation();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
            if (isPlotWorld(l)) {
                PWE.removeMask(pp);
            }
            return;
        }
        if (isPlotWorld(l)) {
            final com.intellectualcrafters.plot.object.Location loc = BukkitUtil.getLocation(l);
            PWE.setMask(pp, loc, false);
        } else {
            PWE.removeMask(pp);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent e) {
        final Location t = e.getTo();
        final Location f = e.getFrom();
        final com.intellectualcrafters.plot.object.Location locf = BukkitUtil.getLocation(f);
        final com.intellectualcrafters.plot.object.Location loct = BukkitUtil.getLocation(t);
        if ((locf.getX() != loct.getX()) || (locf.getZ() != loct.getZ())) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!isPlotWorld(t)) {
                return;
            }
            final PlotId idF = MainUtil.getPlotId(locf);
            final PlotId idT = MainUtil.getPlotId(loct);
            if ((idT != null) && !(idF == idT)) {
                if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                    if (!PWE.hasMask(pp)) {
                        return;
                    }
                }
                PWE.setMask(pp, loct, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(final PlayerPortalEvent e) {
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
            return;
        }
        final Location t = e.getTo();
        final Location f = e.getFrom();
        if (t == null) {
            PWE.removeMask(pp);
            return;
        }
        if (isPlotWorld(t)) {
            final com.intellectualcrafters.plot.object.Location loct = BukkitUtil.getLocation(t);
            PWE.setMask(pp, loct, false);
            return;
        }
        if ((f != null) && isPlotWorld(f)) {
            PWE.removeMask(pp);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e) {
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
            if (!PWE.hasMask(pp)) {
                return;
            }
        }
        final Location t = e.getTo();
        final com.intellectualcrafters.plot.object.Location loct = BukkitUtil.getLocation(t);
        final Location f = e.getFrom();
        if (!PlotSquared.isPlotWorld(loct.getWorld())) {
            if (isPlotWorld(f)) {
                PWE.removeMask(pp);
            }
            return;
        }

        PWE.setMask(pp, loct, false);
    }
}
