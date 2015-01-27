package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;

public class ClusterManager {
	public static HashMap<String, HashSet<PlotCluster>> clusters;
	private static PlotCluster last;
	
	public static boolean contains(PlotCluster cluster, PlotId id) {
		if (cluster.getP1().x <= id.x && cluster.getP1().y <= id.y && cluster.getP2().x >= id.x && cluster.getP2().y >= id.y) {
			return true;
		}
		return false;
	}
	
	public static boolean contains(PlotCluster cluster, Location loc) {
		String world = loc.getWorld().getName();
		PlotManager manager = PlotMain.getPlotManager(world);
		PlotWorld plotworld = PlotMain.getWorldSettings(world);
		Location bot = manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
		Location top = manager.getPlotTopLocAbs(plotworld, cluster.getP2()).add(1,0,1);
		if (bot.getBlockX() < loc.getBlockX() && bot.getBlockZ() < loc.getBlockZ() && top.getBlockX() > loc.getBlockX() && top.getBlockZ() > loc.getBlockZ()) {
			return true;
		}
		return false;
	}
	
	public static PlotCluster getCluster(Plot plot) {
		return getCluster(plot.world, plot.id);
	}
	
	public static PlotCluster getCluster(Location loc) {
		String world = loc.getWorld().getName();
		if (last != null && last.world.equals(world)) {
			if (contains(last, loc)) {
				return last;
			}
		}
		if (clusters == null) {
			return null;
		}
		HashSet<PlotCluster> local = clusters.get(world);
		if (local == null) {
			return null;
		}
		for (PlotCluster cluster : local) {
			if (contains(cluster, loc)) {
				last = cluster;
				return cluster;
			}
		}
		return null;
	}
	
	public static PlotCluster getCluster(String world, PlotId id) {
		if (last != null && last.world.equals(world)) {
			if (contains(last, id)) {
				return last;
			}
		}
		if (clusters == null) {
			return null;
		}
		HashSet<PlotCluster> local = clusters.get(world);
		if (local == null) {
			return null;
		}
		for (PlotCluster cluster : local) {
			if (contains(cluster, id)) {
				last = cluster;
				return cluster;
			}
		}
		return null;
	}

	public static boolean removeCluster(PlotCluster cluster) {
		if (clusters != null) {
			if (clusters.containsKey(cluster.world)) {
				clusters.get(cluster.world).remove(cluster);
				return true;
			}
		}
		return false;
	}
	
	public static PlotClusterId getClusterId(PlotCluster cluster) {
		return new PlotClusterId(cluster.getP1(), cluster.getP2());
	}
}