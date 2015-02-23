package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.SetBlockManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class ClusterManager {
    public static HashMap<String, HashSet<PlotCluster>> clusters;
    private static HashSet<String> regenerating = new HashSet<>();
    public static PlotCluster last;

    public static boolean contains(final PlotCluster cluster, final PlotId id) {
        if ((cluster.getP1().x <= id.x) && (cluster.getP1().y <= id.y) && (cluster.getP2().x >= id.x) && (cluster.getP2().y >= id.y)) {
            return true;
        }
        return false;
    }

    public static HashSet<PlotCluster> getClusters(final World world) {
        return getClusters(world.getName());
    }

    public static HashSet<PlotCluster> getClusters(final String world) {
        if (clusters.containsKey(world)) {
            return clusters.get(world);
        }
        return new HashSet<>();
    }

    public static Location getHome(final PlotCluster cluster) {
        final BlockLoc home = cluster.settings.getPosition();
        Location toReturn;
        if (home.y == 0) {
            // default pos
            final PlotId center = getCenterPlot(cluster);
            toReturn = MainUtil.getPlotHome(cluster.world, center);
            if (toReturn.getY() == 0) {
                final PlotManager manager = PlotSquared.getPlotManager(cluster.world);
                final PlotWorld plotworld = PlotSquared.getPlotWorld(cluster.world);
                final Location loc = manager.getSignLoc(plotworld, MainUtil.getPlot(cluster.world, center));
                toReturn.setY(loc.getY());
            }
        } else {
            toReturn = getClusterBottom(cluster).add(home.x, home.y, home.z);
        }
        final int max = BukkitUtil.getHeighestBlock(cluster.world, toReturn.getX(), toReturn.getZ());
        if (max > toReturn.getY()) {
            toReturn.setY(max);
        }
        return toReturn;
    }

    public static PlotId getCenterPlot(final PlotCluster cluster) {
        final PlotId bot = cluster.getP1();
        final PlotId top = cluster.getP2();
        return new PlotId((bot.x + top.x) / 2, (bot.y + top.y) / 2);
    }

    public static Location getClusterBottom(final PlotCluster cluster) {
        final String world = cluster.world;
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        return manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
    }

    public static Location getClusterTop(final PlotCluster cluster) {
        final String world = cluster.world;
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        final PlotManager manager = PlotSquared.getPlotManager(world);
        return manager.getPlotTopLocAbs(plotworld, cluster.getP2());
    }

    public static PlotCluster getCluster(final String world, final String name) {
        if (!clusters.containsKey(world)) {
            return null;
        }
        for (final PlotCluster cluster : clusters.get(world)) {
            if (cluster.getName().equals(name)) {
                return cluster;
            }
        }
        return null;
    }

    public static boolean contains(final PlotCluster cluster, final Location loc) {
        final String world = loc.getWorld();
        final PlotManager manager = PlotSquared.getPlotManager(world);
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        final Location bot = manager.getPlotBottomLocAbs(plotworld, cluster.getP1());
        final Location top = manager.getPlotTopLocAbs(plotworld, cluster.getP2()).add(1, 0, 1);
        if ((bot.getX() < loc.getX()) && (bot.getZ() < loc.getZ()) && (top.getX() > loc.getX()) && (top.getZ() > loc.getZ())) {
            return true;
        }
        return false;
    }

    public static HashSet<PlotCluster> getIntersects(final String world, final PlotClusterId id) {
        if (!clusters.containsKey(world)) {
            return new HashSet<>();
        }
        final HashSet<PlotCluster> list = new HashSet<PlotCluster>();
        for (final PlotCluster cluster : clusters.get(world)) {
            if (intersects(cluster, id)) {
                list.add(cluster);
            }
        }
        return list;
    }

    public static boolean intersects(final PlotCluster cluster, final PlotClusterId id) {
        final PlotId pos1 = cluster.getP1();
        final PlotId pos2 = cluster.getP2();
        if ((pos1.x <= id.pos2.x) && (pos2.x >= id.pos1.x) && (pos1.y <= id.pos2.y) && (pos2.y >= id.pos1.y)) {
            return true;
        }
        return false;
    }

    public static PlotCluster getCluster(final Plot plot) {
        return getCluster(plot.world, plot.id);
    }

    public static PlotCluster getCluster(final Location loc) {
        final String world = loc.getWorld();
        if ((last != null) && last.world.equals(world)) {
            if (contains(last, loc)) {
                return last;
            }
        }
        if (clusters == null) {
            return null;
        }
        final HashSet<PlotCluster> local = clusters.get(world);
        if (local == null) {
            return null;
        }
        for (final PlotCluster cluster : local) {
            if (contains(cluster, loc)) {
                last = cluster;
                return cluster;
            }
        }
        return null;
    }

    public static PlotCluster getCluster(final String world, final PlotId id) {
        if ((last != null) && last.world.equals(world)) {
            if (contains(last, id)) {
                return last;
            }
        }
        if (clusters == null) {
            return null;
        }
        final HashSet<PlotCluster> local = clusters.get(world);
        if (local == null) {
            return null;
        }
        for (final PlotCluster cluster : local) {
            if (contains(cluster, id)) {
                last = cluster;
                return cluster;
            }
        }
        return null;
    }

    public static boolean removeCluster(final PlotCluster cluster) {
        if (clusters != null) {
            if (clusters.containsKey(cluster.world)) {
                clusters.get(cluster.world).remove(cluster);
                return true;
            }
        }
        return false;
    }

    public static PlotClusterId getClusterId(final PlotCluster cluster) {
        return new PlotClusterId(cluster.getP1(), cluster.getP2());
    }

    public static AugmentedPopulator getPopulator(final PlotCluster cluster) {
        final World world = Bukkit.getWorld(cluster.world);
        for (final BlockPopulator populator : world.getPopulators()) {
            if (populator instanceof AugmentedPopulator) {
                if (((AugmentedPopulator) populator).cluster.equals(cluster)) {
                    return (AugmentedPopulator) populator;
                }
            }
        }
        return null;
    }

    public static PlotId estimatePlotId(final Location loc) {
        final PlotId a = new PlotId(0, 0);
        final PlotId b = new PlotId(1, 1);
        int xw;
        int zw;
        final String world = loc.getWorld();
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        if (plotworld == null) {
            xw = 39;
            zw = 39;
        } else {
            final PlotManager manager = PlotSquared.getPlotManager(world);
            final Location al = manager.getPlotBottomLocAbs(plotworld, a);
            final Location bl = manager.getPlotBottomLocAbs(plotworld, b);
            xw = bl.getX() - al.getX();
            zw = bl.getZ() - al.getZ();
        }
        final int x = loc.getX();
        final int z = loc.getZ();
        return new PlotId((x / xw) + 1, (z / zw) + 1);
    }

    public static void regenCluster(final PlotCluster cluster) {
        if (regenerating.contains(cluster.world + ":" + cluster.getName())) {
            return;
        }
        regenerating.add(cluster.world + ":" + cluster.getName());
        final int interval = 1;
        int i = 0;
        final Random rand = new Random();
        final World world = Bukkit.getWorld(cluster.world);
        final PlotWorld plotworld = PlotSquared.getPlotWorld(cluster.world);
        final Location bot = getClusterBottom(cluster);
        final Location top = getClusterTop(cluster);
        final int minChunkX = bot.getX() >> 4;
        final int maxChunkX = (top.getX() >> 4) + 1;
        final int minChunkZ = bot.getZ() >> 4;
        final int maxChunkZ = (top.getZ() >> 4) + 1;
        final AugmentedPopulator populator = getPopulator(cluster);
        final ArrayList<Chunk> chunks = new ArrayList<>();
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                ClusterManager.regenerating.remove(cluster.world + ":" + cluster.getName());
                final PlotPlayer owner = UUIDHandler.getPlayer(cluster.owner);
                if (owner != null) {
                    MainUtil.sendMessage(owner, C.CLEARING_DONE);
                }
            }
        }, (interval * chunks.size()) + 20);
        // chunks
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunks.add(chunk);
            }
        }
        for (final Chunk chunk : chunks) {
            i += interval;
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if ((populator == null) || (plotworld.TYPE == 0)) {
                        SetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
                        world.regenerateChunk(chunk.getX(), chunk.getZ());
                        chunk.unload(true, true);
                    } else {
                        populator.populate(world, rand, chunk);
                    }
                }
            }, i);
        }
    }
}
