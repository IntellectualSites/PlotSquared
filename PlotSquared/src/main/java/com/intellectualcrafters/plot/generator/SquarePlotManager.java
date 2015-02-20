package com.intellectualcrafters.plot.generator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.ChunkManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;

/**
 * A plot manager with a square grid layout, with square shaped plots
 */
public abstract class SquarePlotManager extends GridPlotManager {
    @Override
    public boolean clearPlot(final World world, final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whendone) {
        final Location pos1 = MainUtil.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(world, plot.id);
        ChunkManager.regenerateRegion(pos1, pos2, whendone);
        return true;
    }
    
    @Override
    public Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        return new Location(Bukkit.getWorld(plotworld.worldname), x, 256, z);
    }
    
    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, final Location loc) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        // get x,z loc
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        // get plot size
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        // get size of path on bottom part, and top part of plot
        // (As 0,0 is in the middle of a road, not the very start)
        int pathWidthLower;
        if ((dpw.ROAD_WIDTH % 2) == 0) {
            pathWidthLower = (int) (Math.floor(dpw.ROAD_WIDTH / 2) - 1);
        } else {
            pathWidthLower = (int) Math.floor(dpw.ROAD_WIDTH / 2);
        }
        // calulating how many shifts need to be done
        int dx = x / size;
        int dz = z / size;
        if (x < 0) {
            dx--;
            x += ((-dx) * size);
        }
        if (z < 0) {
            dz--;
            z += ((-dz) * size);
        }
        // reducing to first plot
        final int rx = (x) % size;
        final int rz = (z) % size;
        // checking if road (return null if so)
        final int end = pathWidthLower + dpw.PLOT_WIDTH;
        final boolean northSouth = (rz <= pathWidthLower) || (rz > end);
        final boolean eastWest = (rx <= pathWidthLower) || (rx > end);
        if (northSouth || eastWest) {
            return null;
        }
        // returning the plot id (based on the number of shifts required)
        return new PlotId(dx + 1, dz + 1);
    }
    
    @Override
    public PlotId getPlotId(final PlotWorld plotworld, final Location loc) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        if (plotworld == null) {
            return null;
        }
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int pathWidthLower;
        if ((dpw.ROAD_WIDTH % 2) == 0) {
            pathWidthLower = (int) (Math.floor(dpw.ROAD_WIDTH / 2) - 1);
        } else {
            pathWidthLower = (int) Math.floor(dpw.ROAD_WIDTH / 2);
        }
        int dx = x / size;
        int dz = z / size;
        if (x < 0) {
            dx--;
            x += ((-dx) * size);
        }
        if (z < 0) {
            dz--;
            z += ((-dz) * size);
        }
        final int rx = (x) % size;
        final int rz = (z) % size;
        final int end = pathWidthLower + dpw.PLOT_WIDTH;
        final boolean northSouth = (rz <= pathWidthLower) || (rz > end);
        final boolean eastWest = (rx <= pathWidthLower) || (rx > end);
        if (northSouth && eastWest) {
            // This means you are in the intersection
            final PlotId id = BukkitPlayerFunctions.getPlotAbs(loc.add(dpw.ROAD_WIDTH, 0, dpw.ROAD_WIDTH));
            final Plot plot = PlotSquared.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.settings.getMerged(0) && plot.settings.getMerged(3))) {
                return BukkitPlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var
            // poorly)
            final PlotId id = BukkitPlayerFunctions.getPlotAbs(loc.add(0, 0, dpw.ROAD_WIDTH));
            final Plot plot = PlotSquared.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(0)) {
                return BukkitPlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final PlotId id = BukkitPlayerFunctions.getPlotAbs(loc.add(dpw.ROAD_WIDTH, 0, 0));
            final Plot plot = PlotSquared.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(3)) {
                return BukkitPlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(dx + 1, dz + 1);
        final Plot plot = PlotSquared.getPlots(loc.getWorld()).get(id);
        if (plot == null) {
            return id;
        }
        return BukkitPlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
    }
    
    /**
     * Get the bottom plot loc (some basic math)
     */
    @Override
    public Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        return new Location(Bukkit.getWorld(plotworld.worldname), x, 1, z);
    }
    
    /**
     * Set a plot biome
     */
    @Override
    public boolean setBiome(final World world, final Plot plot, final Biome biome) {
        final int bottomX = MainUtil.getPlotBottomLoc(world, plot.id).getBlockX() - 1;
        final int topX = MainUtil.getPlotTopLoc(world, plot.id).getBlockX() + 1;
        final int bottomZ = MainUtil.getPlotBottomLoc(world, plot.id).getBlockZ() - 1;
        final int topZ = MainUtil.getPlotTopLoc(world, plot.id).getBlockZ() + 1;
        final Block block = world.getBlockAt(MainUtil.getPlotBottomLoc(world, plot.id).add(1, 1, 1));
        final Biome current = block.getBiome();
        if (biome.equals(current)) {
            return false;
        }
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                final Block blk = world.getBlockAt(x, 0, z);
                final Biome c = blk.getBiome();
                if (c.equals(biome)) {
                    x += 15;
                    continue;
                }
                blk.setBiome(biome);
            }
        }
        return true;
    }
}
