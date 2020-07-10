/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.RegionManager;
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
                final Location pos1 = Location.at(plot.getWorldName(), region.getMinimumPoint());
                final Location pos2 = Location.at(plot.getWorldName(), region.getMaximumPoint());
                RegionManager.manager.regenerateRegion(pos1, pos2, false, this);
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
        return Location.at(squarePlotWorld.getWorldName(), x, Math.min(getWorldHeight(), 255), z);
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
            PlotSquared.debug(
                "Invalid plot / road width in settings.yml for world: " + squarePlotWorld
                    .getWorldName());
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
        return Location.at(squarePlotWorld.getWorldName(), x, squarePlotWorld.getMinBuildHeight(), z);
    }
}
