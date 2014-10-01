package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.database.DBFunc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

/**
 * 
 * @author Citymonstret
 * 
 */
public class WorldEditListener implements Listener {

    private boolean isPlotWorld(Location l) {
        return (PlotMain.isPlotWorld(l.getWorld()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return; 
        }
        Location f = e.getFrom();
        Location t = e.getTo();
        boolean cm = false;
        Player p = e.getPlayer();
        if (t == null) {
            PWE.removeMask(p);
        } else {
            if (f != null) {
                if (!f.getWorld().getName().equalsIgnoreCase(t.getWorld().getName())) {
                    cm = true;
                } else if ((f.getBlockX() != t.getBlockX()) || (f.getBlockZ() != t.getBlockZ())) {
                    PlotId idF = PlayerFunctions.getPlot(f);
                    PlotId idT = PlayerFunctions.getPlot(t);
                    if (!(idF == idT)) {
                        cm = true;
                    }
                }
            }
            if (cm) {
                if (isPlotWorld(t)) {
                    PWE.setMask(p, p.getLocation());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return;
        }
        Player p = e.getPlayer();
        if (isPlotWorld(p.getLocation())) {
            PWE.setMask(p, p.getLocation());
        } else {
            PWE.removeMask(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return;
        }
        Player p = e.getPlayer();
        Location f = e.getFrom(), t = e.getTo();

        if (t == null) {
            PWE.removeMask(p);
        } else {
            if ((f != null) && isPlotWorld(f) && !isPlotWorld(t)) {
                PWE.removeMask(p);
            } else if (isPlotWorld(t)) {
                PWE.setMask(p, p.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return;
        }
        Player p = e.getPlayer();
        Location f = e.getFrom(), t = e.getTo();
        if (t == null) {
            PWE.removeMask(p);
        } else {
            if ((f != null) && isPlotWorld(f) && !isPlotWorld(t)) {
                PWE.removeMask(p);
            } else if (isPlotWorld(t)) {
                PWE.setMask(p, p.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
            return;
        }
        Player p = e.getPlayer();
        if (isPlotWorld(p.getLocation())) {
            String msg = e.getMessage().toLowerCase();
            if (msg.startsWith("//gmask") || msg.startsWith("/gmask") || msg.startsWith("/worldedit:gmask") || msg.startsWith("/worldedit:/gmask")) {
                e.setCancelled(true);
            } else if (msg.startsWith("/up") || msg.startsWith("//up") || msg.startsWith("/worldedit:up") || msg.startsWith("/worldedit:/up")) {
                Plot plot = PlayerFunctions.getCurrentPlot(p);
                if ((p == null) || !(plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId()))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (PlotMain.hasPermission(p, "plots.worldedit.bypass")) {
            return;
        }
        if (!p.hasPermission("plots.admin") && isPlotWorld(p.getLocation())) {
            if (((e.getAction() == Action.LEFT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_BLOCK)) && (p.getItemInHand() != null) && (p.getItemInHand().getType() != Material.AIR)) {
                Block b = e.getClickedBlock();
                Plot plot = PlotHelper.getCurrentPlot(b.getLocation());
                if ((plot != null) && (plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId()))) {
                    PWE.setMask(p, b.getLocation());
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }
}
