////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class DefaultPlotManager extends PlotManager {

    /**
     * Default implementation of getting a plot at a given location For a
     * simplified explanation of the math involved: - Get the current coords -
     * shift these numbers down to something relatable for a single plot
     * (similar to reducing trigonometric functions down to the first quadrant)
     * - e.g. If the plot size is 20 blocks, and we are at x=25, it's equivalent
     * to x=5 for that specific plot From this, and knowing how thick the road
     * is, we can say whether x=5 is road, or plot. The number of shifts done,
     * is also counted, and this number gives us the PlotId
     */
    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, final Location loc) {
        final DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);

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

    /**
     * Some complex stuff for traversing mega plots (return getPlotIdAbs if you
     * do not support mega plots)
     */
    @Override
    public PlotId getPlotId(final PlotWorld plotworld, final Location loc) {
        final DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);

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
            final PlotId id = PlayerFunctions.getPlotAbs(loc.add(dpw.ROAD_WIDTH, 0, dpw.ROAD_WIDTH));
            final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if ((plot.settings.getMerged(0) && plot.settings.getMerged(3))) {
                return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        if (northSouth) {
            // You are on a road running West to East (yeah, I named the var
            // poorly)
            final PlotId id = PlayerFunctions.getPlotAbs(loc.add(0, 0, dpw.ROAD_WIDTH));
            final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(0)) {
                return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        if (eastWest) {
            // This is the road separating an Eastern and Western plot
            final PlotId id = PlayerFunctions.getPlotAbs(loc.add(dpw.ROAD_WIDTH, 0, 0));
            final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
            if (plot == null) {
                return null;
            }
            if (plot.settings.getMerged(3)) {
                return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
            }
            return null;
        }
        final PlotId id = new PlotId(dx + 1, dz + 1);
        final Plot plot = PlotMain.getPlots(loc.getWorld()).get(id);
        if (plot == null) {
            return id;
        }
        return PlayerFunctions.getBottomPlot(loc.getWorld(), plot).id;
    }

    /**
     * Check if a location is inside a specific plot(non-Javadoc) - For this
     * implementation, we don't need to do anything fancier than referring to
     * getPlotIdAbs(...)
     */
    @Override
    public boolean isInPlotAbs(final PlotWorld plotworld, final Location loc, final PlotId plotid) {
        final PlotId result = getPlotIdAbs(plotworld, loc);
        return result != null && result == plotid;
    }

    /**
     * Get the bottom plot loc (some basic math)
     */
    @Override
    public Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);

        final int px = plotid.x;
        final int pz = plotid.y;

        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - dpw.PLOT_WIDTH - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plotworld.worldname), x, 1, z);
    }

    /**
     * Get the top plot loc (some basic math)
     */
    @Override
    public Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final DefaultPlotWorld dpw = ((DefaultPlotWorld) plotworld);

        final int px = plotid.x;
        final int pz = plotid.y;

        final int x = (px * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;
        final int z = (pz * (dpw.ROAD_WIDTH + dpw.PLOT_WIDTH)) - ((int) Math.floor(dpw.ROAD_WIDTH / 2)) - 1;

        return new Location(Bukkit.getWorld(plotworld.worldname), x, 256, z);
    }

    /**
     * Clearing the plot needs to only consider removing the blocks - This
     * implementation has used the SetCuboid function, as it is fast, and uses
     * NMS code - It also makes use of the fact that deleting chunks is a lot
     * faster than block updates This code is very messy, but you don't need to
     * do something quite as complex unless you happen to have 512x512 sized
     * plots
     */
    @Override
    public boolean clearPlot(final World world, final Plot plot) {

        // TODO LOAD CHUNKS TO CLEAR IT PROPERLY

        final DefaultPlotWorld dpw = ((DefaultPlotWorld) PlotMain.getWorldSettings(world));

        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);

        final PlotBlock[] plotfloor = dpw.TOP_BLOCK;
        final PlotBlock[] filling = dpw.MAIN_BLOCK;

        // PlotBlock wall = dpw.WALL_BLOCK;
        final PlotBlock wall = dpw.WALL_BLOCK;

        final PlotBlock wall_filling = dpw.WALL_FILLING;

        Block block = world.getBlockAt(new Location(world, pos1.getBlockX() - 1, 1, pos1.getBlockZ()));
        if ((block.getTypeId() != wall_filling.id) || (block.getData() != wall_filling.data)) {
            setWallFilling(world, dpw, plot.id, wall_filling);
        }

        block = world.getBlockAt(new Location(world, pos1.getBlockX() - 1, dpw.WALL_HEIGHT + 1, pos1.getBlockZ()));
        if ((block.getTypeId() != wall.id) || (block.getData() != wall.data)) {
            setWall(world, dpw, plot.id, wall);
        }

        if ((pos2.getBlockX() - pos1.getBlockX()) < 48) {
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), 0, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, 1, pos2.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT + 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, world.getMaxHeight() + 1, pos2.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT, pos2.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getBlockZ() + 1), plotfloor);
            return true;
        }
        final int startX = (pos1.getBlockX() / 16) * 16;
        final int startZ = (pos1.getBlockZ() / 16) * 16;
        final int chunkX = 16 + pos2.getBlockX();
        final int chunkZ = 16 + pos2.getBlockZ();
        final Location l1 = PlotHelper.getPlotBottomLoc(world, plot.id);
        final Location l2 = PlotHelper.getPlotTopLoc(world, plot.id);
        final int plotMinX = l1.getBlockX() + 1;
        final int plotMinZ = l1.getBlockZ() + 1;
        final int plotMaxX = l2.getBlockX();
        final int plotMaxZ = l2.getBlockZ();
        Location min = null;
        Location max = null;
        for (int i = startX; i < chunkX; i += 16) {
            for (int j = startZ; j < chunkZ; j += 16) {
                final Plot plot1 = PlotHelper.getCurrentPlot(new Location(world, i, 0, j));
                if ((plot1 != null) && (plot1.getId() != plot.getId())) {
                    break;
                }
                final Plot plot2 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j));
                if ((plot2 != null) && (plot2.getId() != plot.getId())) {
                    break;
                }
                final Plot plot3 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j + 15));
                if ((plot3 != null) && (plot3.getId() != plot.getId())) {
                    break;
                }
                final Plot plot4 = PlotHelper.getCurrentPlot(new Location(world, i, 0, j + 15));
                if ((plot4 != null) && (plot4.getId() != plot.getId())) {
                    break;
                }
                final Plot plot5 = PlotHelper.getCurrentPlot(new Location(world, i + 15, 0, j + 15));
                if ((plot5 != null) && (plot5.getId() != plot.getId())) {
                    break;
                }
                if (min == null) {
                    min = new Location(world, Math.max(i - 1, plotMinX), 0, Math.max(j - 1, plotMinZ));
                    max = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                } else if ((max.getBlockZ() < (j + 15)) || (max.getBlockX() < (i + 15))) {
                    max = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                }
                world.regenerateChunk(i / 16, j / 16);
            }
        }

        if (min == null) {
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), 0, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, 1, pos2.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT + 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, world.getMaxHeight() + 1, pos2.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), 1, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT, pos2.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, pos1.getBlockX(), dpw.PLOT_HEIGHT, pos1.getBlockZ()), new Location(world, pos2.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getBlockZ() + 1), plotfloor);
        } else {

            if (min.getBlockX() < plotMinX) {
                min.setX(plotMinX);
            }
            if (min.getBlockZ() < plotMinZ) {
                min.setZ(plotMinZ);
            }
            if (max.getBlockX() > plotMaxX) {
                max.setX(plotMaxX);
            }
            if (max.getBlockZ() > plotMaxZ) {
                max.setZ(plotMaxZ);
            }

            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, plotMinZ), new Location(world, min.getBlockX() + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, plotMinZ), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, plotMinZ), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), 0, plotMinZ), new Location(world, max.getBlockX() + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, max.getBlockX() + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), 1, plotMinZ), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT, min.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, plotMinZ), new Location(world, plotMaxX + 1, 1, min.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, min.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, min.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, min.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, min.getBlockZ()), new Location(world, min.getBlockX() + 1, 1, max.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, min.getBlockZ()), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, max.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, min.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT, max.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, min.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, max.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, 0, max.getBlockZ()), new Location(world, min.getBlockX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, min.getBlockX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, plotMinX, 1, max.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, min.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), 0, max.getBlockZ()), new Location(world, max.getBlockX() + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, max.getBlockX() + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), 1, max.getBlockZ()), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, min.getBlockX(), dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, max.getBlockX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, min.getBlockZ()), new Location(world, plotMaxX + 1, 1, max.getBlockZ() + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, min.getBlockZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, max.getBlockZ() + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, min.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, max.getBlockZ() + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, min.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, max.getBlockZ() + 1), plotfloor);

            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), 0, max.getBlockZ()), new Location(world, plotMaxX + 1, 1, plotMaxZ + 1), new PlotBlock((short) 7, (byte) 0));
            PlotHelper.setSimpleCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT + 1, max.getBlockZ()), new Location(world, plotMaxX + 1, world.getMaxHeight() + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), 1, max.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
            PlotHelper.setCuboid(world, new Location(world, max.getBlockX(), dpw.PLOT_HEIGHT, max.getBlockZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
        }
        return true;
    }

    /**
     * Remove sign for a plot
     */
    @Override
    public Location getSignLoc(final World world, final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        return new Location(world, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX(), dpw.ROAD_HEIGHT + 1, PlotHelper.getPlotBottomLoc(world, plot.id).getBlockZ() - 1);
    }

    @Override
    public boolean setFloor(final World world, final PlotWorld plotworld, final PlotId plotid, final PlotBlock[] blocks) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final Location pos1 = PlotHelper.getPlotBottomLoc(world, plotid).add(1, 0, 1);
        final Location pos2 = PlotHelper.getPlotTopLoc(world, plotid);
        PlotHelper.setCuboid(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), blocks);
        return true;
    }

    @Override
    public boolean setWallFilling(final World w, final PlotWorld plotworld, final PlotId plotid, final PlotBlock plotblock) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        final Location bottom = PlotHelper.getPlotBottomLoc(w, plotid);
        final Location top = PlotHelper.getPlotTopLoc(w, plotid);

        int x, z;

        Block block;

        z = bottom.getBlockZ();
        for (x = bottom.getBlockX(); x < (top.getBlockX() + 1); x++) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                block = w.getBlockAt(x, y, z);
                PlotHelper.setBlock(block, plotblock);
            }
        }

        x = top.getBlockX() + 1;
        for (z = bottom.getBlockZ(); z < (top.getBlockZ() + 1); z++) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                block = w.getBlockAt(x, y, z);
                PlotHelper.setBlock(block, plotblock);
            }
        }

        z = top.getBlockZ() + 1;
        for (x = top.getBlockX() + 1; x > (bottom.getBlockX() - 1); x--) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                block = w.getBlockAt(x, y, z);
                PlotHelper.setBlock(block, plotblock);
            }
        }
        x = bottom.getBlockX();
        for (z = top.getBlockZ() + 1; z > (bottom.getBlockZ() - 1); z--) {
            for (int y = 1; y <= dpw.WALL_HEIGHT; y++) {
                block = w.getBlockAt(x, y, z);
                PlotHelper.setBlock(block, plotblock);
            }
        }
        return true;
    }

    @Override
    public boolean setWall(final World w, final PlotWorld plotworld, final PlotId plotid, final PlotBlock plotblock) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        if (dpw.ROAD_WIDTH == 0) {
            return false;
        }
        final Location bottom = PlotHelper.getPlotBottomLoc(w, plotid);
        final Location top = PlotHelper.getPlotTopLoc(w, plotid);

        int x, z;

        Block block;
        z = bottom.getBlockZ();
        for (x = bottom.getBlockX(); x < (top.getBlockX() + 1); x++) {
            block = w.getBlockAt(x, dpw.WALL_HEIGHT + 1, z);
            PlotHelper.setBlock(block, plotblock);
        }
        x = top.getBlockX() + 1;
        for (z = bottom.getBlockZ(); z < (top.getBlockZ() + 1); z++) {
            block = w.getBlockAt(x, dpw.WALL_HEIGHT + 1, z);
            PlotHelper.setBlock(block, plotblock);
        }
        z = top.getBlockZ() + 1;
        for (x = top.getBlockX() + 1; x > (bottom.getBlockX() - 1); x--) {
            block = w.getBlockAt(x, dpw.WALL_HEIGHT + 1, z);
            PlotHelper.setBlock(block, plotblock);
        }
        x = bottom.getBlockX();
        for (z = top.getBlockZ() + 1; z > (bottom.getBlockZ() - 1); z--) {
            block = w.getBlockAt(x, dpw.WALL_HEIGHT + 1, z);
            PlotHelper.setBlock(block, plotblock);
        }
        return true;
    }

    /**
     * Set a plot biome
     */
    @Override
    public boolean setBiome(final World world, final Plot plot, final Biome biome) {

        final int bottomX = PlotHelper.getPlotBottomLoc(world, plot.id).getBlockX() - 1;
        final int topX = PlotHelper.getPlotTopLoc(world, plot.id).getBlockX() + 1;
        final int bottomZ = PlotHelper.getPlotBottomLoc(world, plot.id).getBlockZ() - 1;
        final int topZ = PlotHelper.getPlotTopLoc(world, plot.id).getBlockZ() + 1;

        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                world.getBlockAt(x, 0, z).setBiome(biome);
            }
        }

        PlotMain.updatePlot(plot);
        PlotHelper.refreshPlotChunks(world, plot);

        return true;
    }

    /**
     * PLOT MERGING
     */

    @Override
    public boolean createRoadEast(final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sx = pos2.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getBlockZ() - 1;
        final int ez = pos2.getBlockZ() + 2;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(w, ex + 1, 257 + 1, ez), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, 1, sz + 1), new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, sx, dpw.WALL_HEIGHT + 1, sz + 1), new Location(w, sx + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, ex, 1, sz + 1), new Location(w, ex + 1, dpw.WALL_HEIGHT + 1, ez), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, ex, dpw.WALL_HEIGHT + 1, sz + 1), new Location(w, ex + 1, dpw.WALL_HEIGHT + 2, ez), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);

        return true;
    }

    @Override
    public boolean createRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sz = pos2.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getBlockX() - 1;
        final int ex = pos2.getBlockX() + 2;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.WALL_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz + 1), new Location(w, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz), new Location(w, ex, dpw.WALL_HEIGHT + 1, sz + 1), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, sz), new Location(w, ex, dpw.WALL_HEIGHT + 2, sz + 1), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, ez), new Location(w, ex, dpw.WALL_HEIGHT + 1, ez + 1), dpw.WALL_FILLING);
        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, dpw.WALL_HEIGHT + 1, ez), new Location(w, ex, dpw.WALL_HEIGHT + 2, ez + 1), dpw.WALL_BLOCK);

        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);

        return true;
    }

    @Override
    public boolean createRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sx = pos2.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos2.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, dpw.ROAD_HEIGHT + 1, sz + 1), new Location(w, ex + 1, 257, ez), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setSimpleCuboid(w, new Location(w, sx + 1, 1, sz + 1), new Location(w, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.ROAD_BLOCK);

        return true;
    }

    @Override
    public boolean removeRoadEast(final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sx = pos2.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = pos1.getBlockZ();
        final int ez = pos2.getBlockZ() + 1;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(w, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.PLOT_HEIGHT, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);

        return true;
    }

    @Override
    public boolean removeRoadSouth(final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final World w = Bukkit.getWorld(plot.world);

        final Location pos1 = getPlotBottomLocAbs(plotworld, plot.id);
        final Location pos2 = getPlotTopLocAbs(plotworld, plot.id);

        final int sz = pos2.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;
        final int sx = pos1.getBlockX();
        final int ex = pos2.getBlockX() + 1;

        PlotHelper.setSimpleCuboid(w, new Location(w, sx, Math.min(dpw.PLOT_HEIGHT, dpw.ROAD_HEIGHT) + 1, sz), new Location(w, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));
        PlotHelper.setCuboid(w, new Location(w, sx, 1, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT, ez + 1), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(w, new Location(w, sx, dpw.PLOT_HEIGHT, sz), new Location(w, ex + 1, dpw.PLOT_HEIGHT + 1, ez + 1), dpw.TOP_BLOCK);

        return true;
    }

    @Override
    public boolean removeRoadSouthEast(final PlotWorld plotworld, final Plot plot) {
        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;
        final World world = Bukkit.getWorld(plot.world);

        final Location loc = getPlotTopLocAbs(dpw, plot.id);

        final int sx = loc.getBlockX() + 1;
        final int ex = (sx + dpw.ROAD_WIDTH) - 1;
        final int sz = loc.getBlockZ() + 1;
        final int ez = (sz + dpw.ROAD_WIDTH) - 1;

        PlotHelper.setSimpleCuboid(world, new Location(world, sx, dpw.ROAD_HEIGHT + 1, sz), new Location(world, ex + 1, 257, ez + 1), new PlotBlock((short) 0, (byte) 0));

        PlotHelper.setCuboid(world, new Location(world, sx + 1, 1, sz + 1), new Location(world, ex, dpw.ROAD_HEIGHT, ez), dpw.MAIN_BLOCK);
        PlotHelper.setCuboid(world, new Location(world, sx + 1, dpw.ROAD_HEIGHT, sz + 1), new Location(world, ex, dpw.ROAD_HEIGHT + 1, ez), dpw.TOP_BLOCK);
        return true;
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot
     * (OPTIONAL)(UNFINISHED)
     */
    @Override
    public boolean finishPlotMerge(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {

        // TODO set plot wall

        final DefaultPlotWorld dpw = (DefaultPlotWorld) plotworld;

        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);

        final PlotBlock block = dpw.WALL_BLOCK;

        final Location megaPlotBot = PlotHelper.getPlotBottomLoc(world, pos1);
        final Location megaPlotTop = PlotHelper.getPlotTopLoc(world, pos2).add(1, 0, 1);
        for (int x = megaPlotBot.getBlockX(); x <= megaPlotTop.getBlockX(); x++) {
            for (int z = megaPlotBot.getBlockZ(); z <= megaPlotTop.getBlockZ(); z++) {
                if ((z == megaPlotBot.getBlockZ()) || (z == megaPlotTop.getBlockZ()) || (x == megaPlotBot.getBlockX()) || (x == megaPlotTop.getBlockX())) {
                    world.getBlockAt(x, dpw.WALL_HEIGHT + 1, z).setTypeIdAndData(block.id, block.data, false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean finishPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean startPlotMerge(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }

    @Override
    public boolean startPlotUnlink(final World world, final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        return true;
    }
}
