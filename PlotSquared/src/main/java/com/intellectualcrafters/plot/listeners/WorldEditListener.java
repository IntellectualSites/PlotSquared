package com.intellectualcrafters.plot.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

import com.intellectualcrafters.plot.PWE;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.database.DBFunc;

/**
 * @author Citymonstret
 */
public class WorldEditListener implements Listener {

	public final Set<String> blockedcmds = new HashSet<String>(Arrays.asList("/gmask", "//gmask", "/worldedit:gmask"));
	public final Set<String> restrictedcmds = new HashSet<String>(Arrays.asList("/up", "//up", "/worldedit:up"));

	private boolean isPlotWorld(Location l) {
		return (PlotMain.isPlotWorld(l.getWorld()));
	}

	@EventHandler(
			priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		if (b == null) {
			return;
		}
		Player p = e.getPlayer();
		Location l = b.getLocation();
		if (!isPlotWorld(l)) {
			return;
		}
		p.getItemInHand();
		if ((p.getItemInHand() == null) || (p.getItemInHand().getType() == Material.AIR)) {
			return;
		}
		Plot plot = PlotHelper.getCurrentPlot(b.getLocation());
		if (plot != null) {
			if ((plot != null) && plot.hasOwner() && (plot.helpers != null)
					&& (plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId()))) {
				PWE.setMask(p, l);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		if (PlotMain.hasPermission(p, "plots.worldedit.bypass") || !PlotMain.isPlotWorld(p.getWorld())) {
			return;
		}
		String cmd = e.getMessage().toLowerCase();

		if (cmd.contains(" ")) {
			cmd = cmd.substring(0, cmd.indexOf(" "));
		}
		if (this.restrictedcmds.contains(cmd)) {
			Plot plot = PlayerFunctions.getCurrentPlot(p);
			if ((plot == null) || !(plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(p.getUniqueId()))) {
				e.setCancelled(true);
			}
		}
		else
			if (this.blockedcmds.contains(cmd)) {
				e.setCancelled(true);
			}
	}

	@EventHandler(
			priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (PlotMain.hasPermission(p, "plots.worldedit.bypass")) {
			return;
		}
		Location l = p.getLocation();
		if (isPlotWorld(l)) {
			PWE.setMask(p, l);
		}
		else {
			PWE.removeMask(p);
		}
	}

	@EventHandler(
			priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e) {
		Location t = e.getTo();
		if (!isPlotWorld(t)) {
			return;
		}
		Location f = e.getFrom();
		Player p = e.getPlayer();

		if ((f.getBlockX() != t.getBlockX()) || (f.getBlockZ() != t.getBlockZ())) {
			PlotId idF = PlayerFunctions.getPlot(f);
			PlotId idT = PlayerFunctions.getPlot(t);
			if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
				return;
			}
			if ((idT != null) && !(idF == idT)) {
				PWE.setMask(p, t);
			}
		}
	}

	@EventHandler(
			priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPortal(PlayerPortalEvent e) {
		if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
			return;
		}
		Player p = e.getPlayer();
		Location t = e.getTo();
		Location f = e.getFrom();
		if (t == null) {
			PWE.removeMask(p);
			return;
		}
		if (isPlotWorld(t)) {
			PWE.setMask(p, t);
			return;
		}
		if (f != null && isPlotWorld(f)) {
			PWE.removeMask(p);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onTeleport(final PlayerTeleportEvent e) {
		if (PlotMain.hasPermission(e.getPlayer(), "plots.worldedit.bypass")) {
			return;
		}
		Player p = e.getPlayer();
		Location t = e.getTo();
		Location f = e.getFrom();
		if (!isPlotWorld(t)) {
			if (isPlotWorld(f)) {
				PWE.removeMask(p);
			}
			else {
				return;
			}
		}
		PWE.setMask(p, t);
	}
}
