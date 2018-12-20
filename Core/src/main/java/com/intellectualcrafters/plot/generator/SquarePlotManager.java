package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A plot manager with a square grid layout, with square shaped plots.
 */
public abstract class SquarePlotManager extends GridPlotManager {

    @Override
    public boolean clearPlot(PlotArea plotArea, final Plot plot, final Runnable whenDone) {
        final HashSet<RegionWrapper> regions = plot.getRegions();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.isEmpty()) {
                    whenDone.run();
                    return;
                }
                Iterator<RegionWrapper> iterator = regions.iterator();
                RegionWrapper region = iterator.next();
                iterator.remove();
                Location pos1 = new Location(plot.getWorldName(), region.minX, region.minY, region.minZ);
                Location pos2 = new Location(plot.getWorldName(), region.maxX, region.maxY, region.maxZ);
                ChunkManager.manager.regenerateRegion(pos1, pos2, false, this);
            }
        };
        run.run();
        return true;
    }

    @Override
    public Location getPlotTopLocAbs(PlotArea plotArea, PlotId plotId) {
        SquarePlotWorld dpw = (SquarePlotWorld) plotArea;
        int px = plotId.x;
        int pz = plotId.y;
        int x = (dpw.ROAD_OFFSET_X + (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - (int) Math.floor(dpw.ROAD_WIDTH / 2) - 1;
        int z = (dpw.ROAD_OFFSET_Z + (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - (int) Math.floor(dpw.ROAD_WIDTH / 2) - 1;
        return new Location(plotArea.worldname, x, Math.min(getWorldHeight(), 255), z);
    }

    @Override
    public PlotId getPlotIdAbs(PlotArea plotArea, int x, int y, int z) {
        SquarePlotWorld dpw = (SquarePlotWorld) plotArea;
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
        int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int idx;
        if (x < 0) {
            idx = x / size;
            x = size + (x % size);
        } else {
            idx = (x / size) + 1;
            x = x % size;
        }
        int idz;
        if (z < 0) {
            idz = z / size;
            z = size + (z % size);
        } else {
            idz = (z / size) + 1;
            z = z % size;
        }
        if (z <= pathWidthLower || z > end || x <= pathWidthLower || x > end) {
            return null;
        } else {
            return new PlotId(idx, idz);
        }
    }

    public PlotId getNearestPlotId(PlotArea plotArea, int x, int y, int z) {
        SquarePlotWorld dpw = (SquarePlotWorld) plotArea;
        if (dpw.ROAD_OFFSET_X != 0) {
            x -= dpw.ROAD_OFFSET_X;
        }
        if (dpw.ROAD_OFFSET_Z != 0) {
            z -= dpw.ROAD_OFFSET_Z;
        }
        int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
        int idx;
        if (x < 0) {
            idx = x / size;
        } else {
            idx = (x / size) + 1;
        }
        int idz;
        if (z < 0) {
            idz = z / size;
        } else {
            idz = (z / size) + 1;
        }
        return new PlotId(idx, idz);
    }

    @Override
    public PlotId getPlotId(PlotArea plotArea, int x, int y, int z) {
        try {
            SquarePlotWorld dpw = (SquarePlotWorld) plotArea;
            if (plotArea == null) {
                return null;
            }
            x -= dpw.ROAD_OFFSET_X;
            z -= dpw.ROAD_OFFSET_Z;
            int size = dpw.PLOT_WIDTH + dpw.ROAD_WIDTH;
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
            int dx;
            int rx;
            if (x < 0) {
                dx = x / size;
                rx = size + (x % size);
            } else {
                dx = (x / size) + 1;
                rx = x % size;
            }
            int dz;
            int rz;
            if (z < 0) {
                dz = z / size;
                rz = size + (z % size);
            } else {
                dz = (z / size) + 1;
                rz = z % size;
            }
            PlotId id = new PlotId(dx, dz);
            boolean[] merged = new boolean[]{rz <= pathWidthLower, rx > end, rz > end, rx <= pathWidthLower};
            int hash = MainUtil.hash(merged);
            // Not merged, and no need to check if it is
            if (hash == 0) {
                return id;
            }
            Plot plot = plotArea.getOwnedPlotAbs(id);
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
            PS.debug("invalid location: " + Arrays.toString(merged));
        } catch (Exception ignored) {
            PS.debug("Invalid plot / road width in settings.yml for world: " + plotArea.worldname);
        }
        return null;
    }

    /**
     * Get the bottom plot loc (some basic math).
     */
    @Override
    public Location getPlotBottomLocAbs(PlotArea plotArea, PlotId plotId) {
        SquarePlotWorld dpw = (SquarePlotWorld) plotArea;
        int px = plotId.x;
        int pz = plotId.y;
        int x = (dpw.ROAD_OFFSET_X + (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - dpw.PLOT_WIDTH - (int) Math.floor(dpw.ROAD_WIDTH / 2);
        int z = (dpw.ROAD_OFFSET_Z + (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH))) - dpw.PLOT_WIDTH - (int) Math.floor(dpw.ROAD_WIDTH / 2);
        return new Location(plotArea.worldname, x, plotArea.MIN_BUILD_HEIGHT, z);
    }
}
