/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.HashUtil;
import com.plotsquared.core.util.RegionManager;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.Set;

/**
 * A plot manager with a square grid layout, with square shaped plots.
 */
public abstract class SquarePlotManager extends GridPlotManager {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + SquarePlotManager.class.getSimpleName());

    private final SquarePlotWorld squarePlotWorld;
    private final RegionManager regionManager;

    public SquarePlotManager(final @NonNull SquarePlotWorld squarePlotWorld, final @NonNull RegionManager regionManager) {
        super(squarePlotWorld);
        this.squarePlotWorld = squarePlotWorld;
        this.regionManager = regionManager;
    }

    @Override
    public boolean clearPlot(
            final @NonNull Plot plot,
            final @Nullable Runnable whenDone,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        final Set<CuboidRegion> regions = plot.getRegions();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (regions.isEmpty()) {
                    if (whenDone != null) {
                        whenDone.run();
                    }
                    return;
                }
                Iterator<CuboidRegion> iterator = regions.iterator();
                CuboidRegion region = iterator.next();
                iterator.remove();
                final Location pos1 = Location.at(plot.getWorldName(), region.getMinimumPoint());
                final Location pos2 = Location.at(plot.getWorldName(), region.getMaximumPoint());
                regionManager.regenerateRegion(pos1, pos2, false, this);
            }
        };
        run.run();
        return true;
    }

    @Override
    public Location getPlotTopLocAbs(@NonNull PlotId plotId) {
        int px = plotId.getX();
        int pz = plotId.getY();
        int x = (squarePlotWorld.ROAD_OFFSET_X + (px * (squarePlotWorld.ROAD_WIDTH + squarePlotWorld.PLOT_WIDTH))) - (int) Math
                .floor(squarePlotWorld.ROAD_WIDTH / 2) - 1;
        int z = (squarePlotWorld.ROAD_OFFSET_Z + (pz * (squarePlotWorld.ROAD_WIDTH + squarePlotWorld.PLOT_WIDTH))) - (int) Math
                .floor(squarePlotWorld.ROAD_WIDTH / 2) - 1;
        return Location.at(squarePlotWorld.getWorldName(), x, squarePlotWorld.getMaxGenHeight(), z);
    }

    @Override
    public PlotId getPlotIdAbs(int x, int y, int z) {
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
        int dx = Math.floorDiv(x, size) + 1;
        int rx = Math.floorMod(x, size);
        int dz = Math.floorDiv(z, size) + 1;
        int rz = Math.floorMod(z, size);
        if (rz <= pathWidthLower || rz > end || rx <= pathWidthLower || rx > end) {
            return null;
        } else {
            return PlotId.of(dx, dz);
        }
    }

    public PlotId getNearestPlotId(@NonNull PlotArea plotArea, int x, int y, int z) {
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
        return PlotId.of(idx, idz);
    }

    @Override
    public PlotId getPlotId(int x, int y, int z) {
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
            int dx = Math.floorDiv(x, size) + 1;
            int rx = Math.floorMod(x, size);
            int dz = Math.floorDiv(z, size) + 1;
            int rz = Math.floorMod(z, size);
            PlotId id = PlotId.of(dx, dz);
            boolean[] merged = new boolean[]{rz <= pathWidthLower, rx > end, rz > end, rx <= pathWidthLower};
            int hash = HashUtil.hash(merged);
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
                case 8 -> {
                    // north
                    return plot.isMerged(Direction.NORTH) ? id : null;
                }
                case 4 -> {
                    // east
                    return plot.isMerged(Direction.EAST) ? id : null;
                }
                case 2 -> {
                    // south
                    return plot.isMerged(Direction.SOUTH) ? id : null;
                }
                case 1 -> {
                    // west
                    return plot.isMerged(Direction.WEST) ? id : null;
                }
                case 12 -> {
                    // northeast
                    return plot.isMerged(Direction.NORTHEAST) ? id : null;
                }
                case 6 -> {
                    // southeast
                    return plot.isMerged(Direction.SOUTHEAST) ? id : null;
                }
                case 3 -> {
                    // southwest
                    return plot.isMerged(Direction.SOUTHWEST) ? id : null;
                }
                case 9 -> {
                    // northwest
                    return plot.isMerged(Direction.NORTHWEST) ? id : null;
                }
            }
        } catch (Exception ignored) {
            LOGGER.error("Invalid plot / road width in settings.yml for world: {}", squarePlotWorld.getWorldName());
        }
        return null;
    }

    /**
     * Get the bottom plot loc (some basic math).
     */
    @Override
    public Location getPlotBottomLocAbs(@NonNull PlotId plotId) {
        int px = plotId.getX();
        int pz = plotId.getY();
        int x = (squarePlotWorld.ROAD_OFFSET_X + (px * (squarePlotWorld.ROAD_WIDTH + squarePlotWorld.PLOT_WIDTH))) - squarePlotWorld.PLOT_WIDTH
                - (int) Math.floor(squarePlotWorld.ROAD_WIDTH / 2);
        int z = (squarePlotWorld.ROAD_OFFSET_Z + (pz * (squarePlotWorld.ROAD_WIDTH + squarePlotWorld.PLOT_WIDTH))) - squarePlotWorld.PLOT_WIDTH
                - (int) Math.floor(squarePlotWorld.ROAD_WIDTH / 2);
        return Location.at(squarePlotWorld.getWorldName(), x, squarePlotWorld.getMinGenHeight(), z);
    }

}
