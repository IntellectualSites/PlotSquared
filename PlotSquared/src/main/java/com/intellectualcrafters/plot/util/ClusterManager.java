package com.intellectualcrafters.plot.util;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.BlockLoc;
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
	
	public static HashSet<PlotCluster> getClusters(World world) {
	    return getClusters(world.getName());
	}
	
	public static HashSet<PlotCluster> getClusters(String world) {
	    if (clusters.containsKey(world)) {
	        return clusters.get(world);
	    }
	    return new HashSet<>();
	}
	
	public static Location getHome(PlotCluster cluster) {
	    World world = Bukkit.getWorld(cluster.world);
	    BlockLoc home = cluster.settings.getPosition();
	    Location toReturn;
	    if (home.y == 0) {
	        // default pos
	        PlotId center = getCenterPlot(cluster);
	        toReturn = PlotHelper.getPlotHome(world, center); 
	        if (toReturn.getBlockY() == 0) {
	            final PlotManager manager = PlotMain.getPlotManager(world);
	            final PlotWorld plotworld = PlotMain.getWorldSettings(world);
	            final Location loc = manager.getSignLoc(world, plotworld, PlotHelper.getPlot(world, center));
	            toReturn.setY(loc.getY());
	        }
	    }
	    else {
	        toReturn = getClusterBottom(cluster).add(home.x, home.y, home.z);
	    }
	    int max = world.getHighestBlockAt(toReturn).getY();
	    if (max > toReturn.getBlockY()) {
	        toReturn.setY(max);
	    }
	    return toReturn;
	}
	
	public static PlotId getCenterPlot(PlotCluster cluster) {
	    PlotId bot = cluster.getP1();
	    PlotId top = cluster.getP2();
	    return new PlotId((bot.x + top.x) / 2, (bot.y + top.y) / 2);
	}
	
	public static Location getClusterBottom(PlotCluster cluster) {
        String world = cluster.world;
        final PlotWorld plotworld = PlotMain.getWorldSettings(world);
        final PlotManager manager = PlotMain.getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
    }
	
	public static Location getClusterTop(PlotCluster cluster) {
        String world = cluster.world;
        final PlotWorld plotworld = PlotMain.getWorldSettings(world);
        final PlotManager manager = PlotMain.getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, cluster.getP2());
    }
	
	public static PlotCluster getCluster(String world, String name) {
	    if (!clusters.containsKey(world)) {
	        return null;
	    }
	    for (PlotCluster cluster : clusters.get(world)) {
	        if (cluster.getName().equals(name)) {
	            return cluster;
	        }
	    }
	    return null;
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
	
	public static HashSet<PlotCluster> getIntersects(String world, PlotClusterId id) {
	    if (!clusters.containsKey(world)) {
	        return new HashSet<>();
	    }
	    HashSet<PlotCluster> list = new HashSet<PlotCluster>();
	    for (PlotCluster cluster : clusters.get(world)) {
	        if (intersects(cluster, id)) {
	            list.add(cluster);
	        }
	    }
	    return list;
	}
	
	public static boolean intersects(PlotCluster cluster, PlotClusterId id) {
	    PlotId pos1 = cluster.getP1();
        PlotId pos2 = cluster.getP2();
	    if (pos1.x <= id.pos2.x && pos2.x >= id.pos1.x && pos1.y <= id.pos2.y && pos2.y >= id.pos1.y) {
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