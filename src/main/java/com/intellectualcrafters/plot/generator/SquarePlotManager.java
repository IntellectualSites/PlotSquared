package com.intellectualcrafters.plot.generator;

import java.util.HashSet;
import java.util.Iterator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;

/**
 * A plot manager with a square grid layout, with square shaped plots
 */
public abstract class SquarePlotManager extends GridPlotManager {
    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final Runnable whenDone) {
        final HashSet<RegionWrapper> regions = MainUtil.getRegions(plot);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.size() == 0) {
                    whenDone.run();
                    return;
                }
                Iterator<RegionWrapper> iter = regions.iterator();
                RegionWrapper region = iter.next();
                iter.remove();
                Location pos1 = new Location(plot.world, region.minX, region.minY, region.minZ);
                Location pos2 = new Location(plot.world, region.maxX, region.maxY, region.maxZ);
                ChunkManager.manager.regenerateRegion(pos1, pos2, this);
            }
        };
        run.run();
        return true;
    }
    
    @Override
    public Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (dpw.ROAD_OFFSET_X + (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (dpw.ROAD_OFFSET_Z + (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        return new Location(plotworld.worldname, x, Math.min(plotworld.MAX_BUILD_HEIGHT, 255), z);
    }
    
    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, int x, final int y, int z) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        if (dpw.ROAD_OFFSET_X != 0) {
            x -= dpw.ROAD_OFFSET_X;
        }
        if (dpw.ROAD_OFFSET_Z != 0) {
            z -= dpw.ROAD_OFFSET_Z;
        }
        int pathWidthLower;
        int end;
        if (dpw.ROAD_WIDTH == 0) {
            pathWidthLower = -1;
            end = dpw.PLOT_WIDTH;
        } else {
            if ((dpw.ROAD_WIDTH % 2) == 0) {
                pathWidthLower = (dpw.ROAD_WIDTH / 2) - 1;
            } else {
                pathWidthLower = dpw.ROAD_WIDTH / 2;
            }
            end = pathWidthLower + dpw.PLOT_WIDTH;
        }
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int idx;
        int idz;
        if (x < 0) {
            idx = (x / size);
            x = size + (x % size);
        } else {
            idx = (x / size) + 1;
            x = (x % size);
        }
        if (z < 0) {
            idz = (z / size);
            z = size + (z % size);
        } else {
            idz = (z / size) + 1;
            z = (z % size);
        }
        return ((z <= pathWidthLower) || (z > end) || (x <= pathWidthLower) || (x > end)) ? null : new PlotId(idx, idz);
    }
    
    @Override
    public PlotId getPlotId(final PlotWorld plotworld, int x, final int y, int z) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        if (plotworld == null) {
            return null;
        }
        x -= dpw.ROAD_OFFSET_X;
        z -= dpw.ROAD_OFFSET_Z;
        final int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int pathWidthLower;
        final int end;
        if (dpw.ROAD_WIDTH == 0) {
            pathWidthLower = -1;
            end = dpw.PLOT_WIDTH;
        } else {
            if ((dpw.ROAD_WIDTH % 2) == 0) {
                pathWidthLower = (dpw.ROAD_WIDTH / 2) - 1;
            } else {
                pathWidthLower = dpw.ROAD_WIDTH / 2;
            }
            end = pathWidthLower + dpw.PLOT_WIDTH;
        }
        int dx;
        int dz;
        int rx;
        int rz;
        if (x < 0) {
            dx = (x / size);
            rx = size + (x % size);
        } else {
            dx = (x / size) + 1;
            rx = (x % size);
        }
        if (z < 0) {
            dz = (z / size);
            rz = size + (z % size);
        } else {
            dz = (z / size) + 1;
            rz = (z % size);
        }
        PlotId id = new PlotId(dx, dz);
        boolean[] merged = new boolean[] {(rz <= pathWidthLower), (rx > end), (rz > end), (rx <= pathWidthLower)};
        int hash = MainUtil.hash(merged);
        // Not merged, and no need to check if it is
        if (hash == 0) {
            return id;
        }
        Plot plot = PS.get().getPlot(plotworld.worldname, id);
        // Not merged, and standing on road
        if (plot == null) {
            return null;
        }
        switch (hash) {
            case 8:
                // north
                return plot.getMerged(0) ? id : null;
            case 4:
                // east
                return plot.getMerged(1) ? id : null;
            case 2:
                // south
                return plot.getMerged(2) ? id : null;
            case 1:
                // west
                return plot.getMerged(3) ? id : null;
            case 12:
                // northest
                return plot.getMerged(4) ? id : null;
            case 6:
                // southeast
                return plot.getMerged(5) ? id : null;
            case 3:
                // southwest
                return plot.getMerged(6) ? id : null;
            case 9:
                // northwest
                return plot.getMerged(7) ? id : null;
        }
        PS.debug("invalid location: " + merged);
        return null;
    }
    
    /**
     * Get the bottom plot loc (some basic math)
     */
    @Override
    public Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SquarePlotWorld dpw = ((SquarePlotWorld) plotworld);
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (dpw.ROAD_OFFSET_X + (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2));
        final int z = (dpw.ROAD_OFFSET_Z + (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2));
        return new Location(plotworld.worldname, x, plotworld.MIN_BUILD_HEIGHT, z);
    }
}
