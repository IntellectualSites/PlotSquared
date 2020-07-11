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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.util.BlockUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * A plot manager with square plots which tessellate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot).
 */
public class ClassicPlotManager extends SquarePlotManager {

    private final ClassicPlotWorld classicPlotWorld;
    private final RegionManager regionManager;

    public ClassicPlotManager(@NotNull final ClassicPlotWorld classicPlotWorld,
                              @NotNull final RegionManager regionManager) {
        super(classicPlotWorld, regionManager);
        this.classicPlotWorld = classicPlotWorld;
        this.regionManager = regionManager;
    }

    @Override public boolean setComponent(PlotId plotId, String component, Pattern blocks) {
        final Optional<ClassicPlotManagerComponent> componentOptional =
            ClassicPlotManagerComponent.fromString(component);
        if (componentOptional.isPresent()) {
            switch (componentOptional.get()) {
                case FLOOR:
                    return setFloor(plotId, blocks);
                case WALL:
                    return setWallFilling(plotId, blocks);
                case AIR:
                    return setAir(plotId, blocks);
                case MAIN:
                    return setMain(plotId, blocks);
                case MIDDLE:
                    return setMiddle(plotId, blocks);
                case OUTLINE:
                    return setOutline(plotId, blocks);
                case BORDER:
                    return setWall(plotId, blocks);
                case ALL:
                    return setAll(plotId, blocks);
            }
        }
        return false;
    }

    @Override public boolean unClaimPlot(Plot plot, Runnable whenDone) {
        setWallFilling(plot.getId(), classicPlotWorld.WALL_FILLING.toPattern());
        if (!classicPlotWorld.WALL_BLOCK.isAir() || !classicPlotWorld.WALL_BLOCK
            .equals(classicPlotWorld.CLAIMED_WALL_BLOCK)) {
            setWall(plot.getId(), classicPlotWorld.WALL_BLOCK.toPattern());
        }
        return PlotSquared.platform().getGlobalBlockQueue().addEmptyTask(whenDone);
    }

    public boolean setFloor(PlotId plotId, Pattern blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot.isBasePlot()) {
            return this.regionManager.setCuboids(classicPlotWorld, plot.getRegions(), blocks,
                classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.PLOT_HEIGHT);
        }
        return false;
    }

    public boolean setAll(PlotId plotId, Pattern blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot.isBasePlot()) {
            return this.regionManager.setCuboids(classicPlotWorld, plot.getRegions(), blocks, 1, getWorldHeight());
        }
        return false;
    }

    public boolean setAir(PlotId plotId, Pattern blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot.isBasePlot()) {
            return this.regionManager.setCuboids(classicPlotWorld, plot.getRegions(), blocks,
                classicPlotWorld.PLOT_HEIGHT + 1, getWorldHeight());
        }
        return false;
    }

    public boolean setMain(PlotId plotId, Pattern blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot.isBasePlot()) {
            return this.regionManager.setCuboids(classicPlotWorld, plot.getRegions(), blocks, 1,
                classicPlotWorld.PLOT_HEIGHT - 1);
        }
        return false;
    }

    public boolean setMiddle(PlotId plotId, Pattern blocks) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (!plot.isBasePlot()) {
            return false;
        }
        Location[] corners = plot.getCorners();
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);

        int x = MathMan.average(corners[0].getX(), corners[1].getX());
        int z = MathMan.average(corners[0].getZ(), corners[1].getZ());
        queue.setBlock(x, classicPlotWorld.PLOT_HEIGHT, z, blocks);
        return queue.enqueue();
    }

    public boolean setOutline(PlotId plotId, Pattern blocks) {
        if (classicPlotWorld.ROAD_WIDTH == 0) {
            return false;
        }
        // When using full vanilla generation, don't generate the walls
        if (classicPlotWorld.getTerrain() == PlotAreaTerrainType.ALL) {
            // Return true because the method actually did what it's intended to in this case,
            // which is absolutely nothing
            return true;
        }
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        Location bottom = plot.getBottomAbs();
        Location top = plot.getExtendedTopAbs();
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int maxY = classicPlotWorld.getPlotManager().getWorldHeight();
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bottom.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bottom.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }

        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (plot.isBasePlot()) {
            for (CuboidRegion region : plot.getRegions()) {
                Location pos1 =
                    Location.at(classicPlotWorld.getWorldName(), region.getMinimumPoint().getX(),
                        maxY, region.getMinimumPoint().getZ());
                Location pos2 =
                    Location.at(classicPlotWorld.getWorldName(), region.getMaximumPoint().getX(),
                        maxY, region.getMaximumPoint().getZ());
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        return queue.enqueue();
    }

    public boolean setWallFilling(PlotId plotId, Pattern blocks) {
        if (classicPlotWorld.ROAD_WIDTH == 0) {
            return false;
        }
        // When using full vanilla generation, don't generate the walls
        if (classicPlotWorld.getTerrain() == PlotAreaTerrainType.ALL) {
            // Return true because the method actually did what it's intended to in this case,
            // which is absolutely nothing
            return true;
        }
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        Location bot = plot.getExtendedBottomAbs()
            .subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0,
                plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX();
                 x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ();
                 z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        return queue.enqueue();
    }

    public boolean setWall(PlotId plotId, Pattern blocks) {
        if (classicPlotWorld.ROAD_WIDTH == 0) {
            return false;
        }
        // When using full vanilla generation, don't generate the walls
        if (classicPlotWorld.getTerrain() == PlotAreaTerrainType.ALL) {
            // Return true because the method actually did what it's intended to in this case,
            // which is absolutely nothing
            return true;
        }
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        Location bot = plot.getExtendedBottomAbs()
            .subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0,
                plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int y = classicPlotWorld.WALL_HEIGHT + 1;
        if (!plot.getMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.getMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.getMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX();
                 x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ();
                 z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        return queue.enqueue();
    }

    /**
     * PLOT MERGING.
     */
    @Override public boolean createRoadEast(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 2;
        int ez = pos2.getZ() + 2;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        int maxY = getWorldHeight();
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx,
                Math.min(classicPlotWorld.WALL_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, maxY, ez - 1),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 0, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, 0, ez - 1),
            BlockUtil.get((short) 7, (byte) 0));
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT, ez - 1),
            classicPlotWorld.WALL_FILLING.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT + 1,
                sz + 1),
            Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT + 1,
                ez - 1), classicPlotWorld.WALL_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), ex, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT, ez - 1),
            classicPlotWorld.WALL_FILLING.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT + 1,
                sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT + 1,
                ez - 1), classicPlotWorld.WALL_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT,
                ez - 1), classicPlotWorld.ROAD_BLOCK.toPattern());
        return queue.enqueue();
    }

    @Override public boolean createRoadSouth(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 2;
        int ex = pos2.getX() + 2;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1,
                Math.min(classicPlotWorld.WALL_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1,
                classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 0, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, 0, ez),
            BlockUtil.get((short) 7, (byte) 0));
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT, sz),
            classicPlotWorld.WALL_FILLING.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.WALL_HEIGHT + 1,
                sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT + 1,
                sz), classicPlotWorld.WALL_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, ez),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT, ez),
            classicPlotWorld.WALL_FILLING.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.WALL_HEIGHT + 1,
                ez),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT + 1,
                ez), classicPlotWorld.WALL_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT,
                ez - 1), classicPlotWorld.ROAD_BLOCK.toPattern());
        return queue.enqueue();
    }

    @Override public boolean createRoadSouthEast(Plot plot) {
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.ROAD_HEIGHT + 1,
                sz + 1), Location.at(classicPlotWorld.getWorldName(), ex - 1,
                classicPlotWorld.getPlotManager().getWorldHeight(), ez - 1),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 0, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, 0, ez - 1),
            BlockUtil.get((short) 7, (byte) 0));
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT,
                ez - 1), classicPlotWorld.ROAD_BLOCK.toPattern());
        return queue.enqueue();
    }

    @Override public boolean removeRoadEast(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 1;
        int ez = pos2.getZ() + 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx,
                Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex,
                classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT - 1,
                ez - 1), classicPlotWorld.MAIN_BLOCK.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.PLOT_HEIGHT, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT, ez - 1),
            classicPlotWorld.TOP_BLOCK.toPattern());
        return queue.enqueue();
    }

    @Override public boolean removeRoadSouth(Plot plot) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 1;
        int ex = pos2.getX() + 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx,
                Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex,
                classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.PLOT_HEIGHT - 1,
                ez), classicPlotWorld.MAIN_BLOCK.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.PLOT_HEIGHT, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.PLOT_HEIGHT, ez),
            classicPlotWorld.TOP_BLOCK.toPattern());
        return queue.enqueue();
    }

    @Override public boolean removeRoadSouthEast(Plot plot) {
        Location location = getPlotTopLocAbs(plot.getId());
        int sx = location.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = location.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        LocalBlockQueue queue = classicPlotWorld.getQueue(false);
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx,
                        Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
                Location.at(classicPlotWorld.getWorldName(), ex,
                        classicPlotWorld.getPlotManager().getWorldHeight(), ez),
                BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT - 1, ez),
            classicPlotWorld.MAIN_BLOCK.toPattern());
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.PLOT_HEIGHT, sz),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT, ez),
            classicPlotWorld.TOP_BLOCK.toPattern());
        return queue.enqueue();
    }

    /**
     * Finishing off plot merging by adding in the walls surrounding the plot (OPTIONAL)(UNFINISHED).
     *
     * @return false if part of the merge failed, otherwise true if successful.
     */
    @Override public boolean finishPlotMerge(List<PlotId> plotIds) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK)) {
            for (PlotId plotId : plotIds) {
                setWall(plotId, claim.toPattern());
            }
        }
        if (Settings.General.MERGE_REPLACE_WALL) {
            final BlockBucket wallBlock = classicPlotWorld.WALL_FILLING;
            for (PlotId id : plotIds) {
                setWallFilling(id, wallBlock.toPattern());
            }
        }
        return true;
    }

    @Override public boolean finishPlotUnlink(List<PlotId> plotIds) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK)) {
            for (PlotId id : plotIds) {
                setWall(id, claim.toPattern());
            }
        }
        return true; // return false if unlink has been denied
    }

    @Override public boolean startPlotMerge(List<PlotId> plotIds) {
        return true;
    }

    @Override public boolean startPlotUnlink(List<PlotId> plotIds) {
        return true;
    }

    @Override public boolean claimPlot(Plot plot) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK)) {
            return setWall(plot.getId(), claim.toPattern());
        }
        return true;
    }

    @Override public String[] getPlotComponents(PlotId plotId) {
        return ClassicPlotManagerComponent.stringValues();
    }

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    @Override public Location getSignLoc(Plot plot) {
        plot = plot.getBasePlot(false);
        final Location bot = plot.getBottomAbs();
        return Location.at(classicPlotWorld.getWorldName(), bot.getX() - 1,
            classicPlotWorld.ROAD_HEIGHT + 1, bot.getZ() - 2);
    }

}
