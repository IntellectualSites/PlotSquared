package com.github.intellectualsites.plotsquared.generator;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Direction;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.util.ChunkManager;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * A plot manager with a square grid layout, with square shaped plots.
 */
public abstract class SquarePlotManager extends GridPlotManager {

    private final SquarePlotWorld squarePlotWorld;

    public SquarePlotManager(SquarePlotWorld squarePlotWorld) {
        super(squarePlotWorld);
        this.squarePlotWorld = squarePlotWorld;
    }

    @Override public boolean clearPlot(final Plot plot, final Runnable whenDone) {
        final Set<CuboidRegion> regions = plot.getRegions();
        Runnable run = new Runnable() {
            @Override public void run() {
                if (regions.isEmpty()) {
                    whenDone.run();
                    return;
                }
                Iterator<CuboidRegion> iterator = regions.iterator();
                CuboidRegion region = iterator.next();
                iterator.remove();
                Location pos1 = new Location(plot.getWorldName(), region.getMinimumPoint().getX(),
                    region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
                Location pos2 = new Location(plot.getWorldName(), region.getMaximumPoint().getX(),
                    region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());
                ChunkManager.manager.regenerateRegion(pos1, pos2, false, this);
            }
        };
        run.run();
        return true;
    }

    @Override public Location getPlotTopLocAbs(PlotId plotId) {
        int px = plotId.x;
        int pz = plotId.y;
        int x = (squarePlotWorld.ROAD_OFFSET_X + (px * (squarePlotWorld.ROAD_WIDTH
            + squarePlotWorld.PLOT_WIDTH))) - (int) Math.floor(squarePlotWorld.ROAD_WIDTH / 2) - 1;
        int z = (squarePlotWorld.ROAD_OFFSET_Z + (pz * (squarePlotWorld.ROAD_WIDTH
            + squarePlotWorld.PLOT_WIDTH))) - (int) Math.floor(squarePlotWorld.ROAD_WIDTH / 2) - 1;
        return new Location(squarePlotWorld.getWorldName(), x, Math.min(getWorldHeight(), 255), z);
    }

    @Override public PlotId getPlotIdAbs(int x, int y, int z) {
        if (squarePlotWorld.ROAD_OFFSET_X != 0) {
            x -= squarePlotWorld.ROAD_OFFSET_X;
        }
        if (squarePlotWorld.ROAD_OFFSET_Z != 0) {
            z -= squarePlotWorld.ROAD_OFFSET_Z;
        }
        int pathWidthLower;
        int end;
        if (squarePlotWorld.ROAD_WIDTH == 0) {
            pathWidthLower = -1;
            end = squarePlotWorld.PLOT_WIDTH;
        } else {
            if ((squarePlotWorld.ROAD_WIDTH % 2) == 0) {
                pathWidthLower = (squarePlotWorld.ROAD_WIDTH / 2) - 1;
            } else {
                pathWidthLower = squarePlotWorld.ROAD_WIDTH / 2;
            }
            end = pathWidthLower + squarePlotWorld.PLOT_WIDTH;
        }
        int size = squarePlotWorld.PLOT_WIDTH + squarePlotWorld.ROAD_WIDTH;
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

    @Override public PlotId getPlotId(int x, int y, int z) {
        try {
            x -= squarePlotWorld.ROAD_OFFSET_X;
            z -= squarePlotWorld.ROAD_OFFSET_Z;
            int size = squarePlotWorld.PLOT_WIDTH + squarePlotWorld.ROAD_WIDTH;
            int pathWidthLower;
            int end;
            if (squarePlotWorld.ROAD_WIDTH == 0) {
                pathWidthLower = -1;
                end = squarePlotWorld.PLOT_WIDTH;
            } else {
                if ((squarePlotWorld.ROAD_WIDTH % 2) == 0) {
                    pathWidthLower = (squarePlotWorld.ROAD_WIDTH / 2) - 1;
                } else {
                    pathWidthLower = squarePlotWorld.ROAD_WIDTH / 2;
                }
                end = pathWidthLower + squarePlotWorld.PLOT_WIDTH;
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
            boolean[] merged =
                new boolean[] {rz <= pathWidthLower, rx > end, rz > end, rx <= pathWidthLower};
            int hash = MainUtil.hash(merged);
            // Not merged, and no need to check if it is
            if (hash == 0) {
                return id;
            }
            Plot plot = squarePlotWorld.getOwnedPlotAbs(id);
            // Not merged, and standing on road
            if (plot == null) {
                return null;
            }
            switch (hash) {
                case 8:
                    // north
                    return plot.getMerged(Direction.NORTH) ? id : null;
                case 4:
                    // east
                    return plot.getMerged(Direction.EAST) ? id : null;
                case 2:
                    // south
                    return plot.getMerged(Direction.SOUTH) ? id : null;
                case 1:
                    // west
                    return plot.getMerged(Direction.WEST) ? id : null;
                case 12:
                    // northeast
                    return plot.getMerged(Direction.NORTHEAST) ? id : null;
                case 6:
                    // southeast
                    return plot.getMerged(Direction.SOUTHEAST) ? id : null;
                case 3:
                    // southwest
                    return plot.getMerged(Direction.SOUTHWEST) ? id : null;
                case 9:
                    // northwest
                    return plot.getMerged(Direction.NORTHWEST) ? id : null;
            }
            PlotSquared.debug("invalid location: " + Arrays.toString(merged));
        } catch (Exception ignored) {
            PlotSquared.debug("Invalid plot / road width in settings.yml for world: "
                + squarePlotWorld.getWorldName());
        }
        return null;
    }

    /**
     * Get the bottom plot loc (some basic math).
     */
    @Override public Location getPlotBottomLocAbs(PlotId plotId) {
        int px = plotId.x;
        int pz = plotId.y;
        int x = (squarePlotWorld.ROAD_OFFSET_X + (px * (squarePlotWorld.ROAD_WIDTH
            + squarePlotWorld.PLOT_WIDTH))) - squarePlotWorld.PLOT_WIDTH - (int) Math
            .floor(squarePlotWorld.ROAD_WIDTH / 2);
        int z = (squarePlotWorld.ROAD_OFFSET_Z + (pz * (squarePlotWorld.ROAD_WIDTH
            + squarePlotWorld.PLOT_WIDTH))) - squarePlotWorld.PLOT_WIDTH - (int) Math
            .floor(squarePlotWorld.ROAD_WIDTH / 2);
        return new Location(squarePlotWorld.getWorldName(), x, squarePlotWorld.getMinBuildHeight(), z);
    }
}
