package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;

public class ClusterManager {
	public static HashMap<String, HashSet<PlotCluster>> clusters;
	private static HashSet<String> regenerating = new HashSet<>();
	public static PlotCluster last;
	
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
	
	public static AugmentedPopulator getPopulator(PlotCluster cluster) {
	    World world = Bukkit.getWorld(cluster.world);
	    for (Iterator<BlockPopulator> iterator = world.getPopulators().iterator(); iterator.hasNext();) {
            BlockPopulator populator = iterator.next();
            if (populator instanceof AugmentedPopulator) {
                if (((AugmentedPopulator) populator).cluster.equals(cluster)) {
                    return (AugmentedPopulator) populator;
                }
            }
        }
	    return null;
	}
	
	public static PlotId estimatePlotId(Location loc) {
	    PlotId a = new PlotId(0, 0);
	    PlotId b = new PlotId(1, 1);
	    int xw;
	    int zw;
	    	    
	    String world = loc.getWorld().getName();
	    PlotWorld plotworld = PlotMain.getWorldSettings(world);
	    if (plotworld == null) {
	        xw = 39;
	        zw = 39;
	    }
	    else {
	        PlotManager manager = PlotMain.getPlotManager(world);
	        Location al = manager.getPlotBottomLocAbs(plotworld, a);
	        Location bl = manager.getPlotBottomLocAbs(plotworld, b);
	        
	        xw = bl.getBlockX() - al.getBlockX();
	        zw = bl.getBlockZ() - al.getBlockZ();
	    }
	    
	    int x = loc.getBlockX();
	    int z = loc.getBlockZ();
	    
	    return new PlotId((x/xw) + 1,(z/zw) + 1);
	}
	
	public static void regenCluster(final PlotCluster cluster) {
	    if (regenerating.contains(cluster.world + ":" + cluster.getName())) {
	        return;
	    }
	    regenerating.add(cluster.world + ":" + cluster.getName());
	    int interval = 1;
	    int i = 0;
	    final Random rand = new Random();
        final World world = Bukkit.getWorld(cluster.world);
        final PlotWorld plotworld = PlotMain.getWorldSettings(cluster.world);
        
	    Location bot = getClusterBottom(cluster);
	    Location top = getClusterTop(cluster);
	    
	    int minChunkX = bot.getBlockX() >> 4;
	    int maxChunkX = (top.getBlockX() >> 4) + 1;
	    int minChunkZ = bot.getBlockZ() >> 4;
        int maxChunkZ = (top.getBlockZ() >> 4) + 1;
	    
	    final AugmentedPopulator populator = getPopulator(cluster);
	    final ArrayList<Chunk> chunks = new ArrayList<>();
	    
	    TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                ClusterManager.regenerating.remove(cluster.world + ":" + cluster.getName());
                Player owner = UUIDHandler.uuidWrapper.getPlayer(cluster.owner);
                if (owner != null) {
                    PlayerFunctions.sendMessage(owner, C.CLEARING_DONE);
                }
            }
        }, interval * chunks.size() + 20);
	    
	    // chunks
	    for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunks.add(chunk);
            }
        }
	    for (final Chunk chunk : chunks) {
	        i+=interval;
	        TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if (populator == null || plotworld.TYPE == 0) {
<<<<<<< Updated upstream
                        AbstractSetBlock.setBlockManager.update(Arrays.asList( new Chunk[] {chunk}));
=======
                        SetBlockManager.setBlockManager.update(Arrays.asList( new Chunk[] {chunk}));
>>>>>>> Stashed changes
                        world.regenerateChunk(chunk.getX(), chunk.getZ());
                        chunk.unload(true, true);
                    }
                    else {
                        populator.populate(world, rand, chunk);
                    }
                }
            }, i);
	    }
	}
}