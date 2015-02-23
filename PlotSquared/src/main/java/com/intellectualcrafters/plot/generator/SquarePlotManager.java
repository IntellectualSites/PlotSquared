package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.AChunkManager;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;

/**
 * A plot manager with a square grid layout, with square shaped plots
 */
public abstract class SquarePlotManager extends GridPlotManager {
    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
        AChunkManager.manager.regenerateRegion(pos1, pos2, whenDone);
        return true;
    }

    @Override
    public Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        return new Location(plotworld.worldname, x, 256, z);
    }

    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, int x, final int y, int z) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
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
    public PlotId getPlotId(final PlotWorld plotworld, int x, final int y, int z) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
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
            final Location loc = new Location(plotworld.worldname, x + dpw.ROAD_WIDTH, y, z + dpw.ROAD_WIDTH);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.settings.getMerged(0) && plot.settings.getMerged(3))) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var poorly)
            final Location loc = new Location(plotworld.worldname, x, y, z + dpw.ROAD_WIDTH);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(0)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final Location loc = new Location(plotworld.worldname, x + dpw.ROAD_WIDTH, y, z);
            final PlotId id = MainUtil.getPlotAbs(loc);
            final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(3)) {
                return MainUtil.getBottomPlot(plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(dx + 1, dz + 1);
        final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
        if (plot == null) {
            return id;
        }
        return MainUtil.getBottomPlot(plot).id;
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
        return new Location(plotworld.worldname, x, 1, z);
    }

    /**
     * Set a plot biome
     */
    @Override
    public boolean setBiome(final Plot plot, final int biome) {
        final int bottomX = MainUtil.getPlotBottomLoc(plot.world, plot.id).getX() - 1;
        final int topX = MainUtil.getPlotTopLoc(plot.world, plot.id).getX() + 1;
        final int bottomZ = MainUtil.getPlotBottomLoc(plot.world, plot.id).getZ() - 1;
        final int topZ = MainUtil.getPlotTopLoc(plot.world, plot.id).getZ() + 1;
        final int size = ((topX - bottomX) + 1) * ((topZ - bottomZ) + 1);
        final int[] xb = new int[size];
        final int[] zb = new int[size];
        final int[] biomes = new int[size];
        int index = 0;
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                xb[index] = x;
                zb[index] = z;
                biomes[index] = biome;
                index++;
            }
        }
        BlockManager.setBiomes(plot.world, xb, zb, biomes);
        return true;
    }
}
