package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.HashSet;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;

public class ClusterManager {
	public static HashMap<String, HashSet<PlotCluster>> clusters;
	private static PlotCluster last;
	
	public static boolean contains(PlotCluster cluster, PlotId id) {
		if (cluster.getP1().x <= id.x && cluster.getP1().y <= id.y && cluster.getP2().x >= id.x && cluster.getP2().y >= id.y) {
			return true;
		}
		return false;
	}
	
	public static PlotCluster getCluster(Plot plot) {
		if (last != null && last.world.equals(plot.world)) {
			if (contains(last, plot.id)) {
				return last;
			}
		}
		if (clusters == null) {
			return null;
		}
		HashSet<PlotCluster> local = clusters.get(plot.world);
		if (local == null) {
			return null;
		}
		PlotId id = plot.id;
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