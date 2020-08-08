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

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.BlockUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A plot manager with square plots which tessellate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot).
 */
public class ClassicPlotManager extends SquarePlotManager {

    private final ClassicPlotWorld classicPlotWorld;
    private final RegionManager regionManager;

    public ClassicPlotManager(@Nonnull final ClassicPlotWorld classicPlotWorld, @Nonnull final RegionManager regionManager) {
        super(classicPlotWorld, regionManager);
        this.classicPlotWorld = classicPlotWorld;
        this.regionManager = regionManager;
    }

    @Override public boolean setComponent(@Nonnull PlotId plotId,
                                          @Nonnull String component,
                                          @Nonnull Pattern blocks,
                                          @Nullable QueueCoordinator queue) {
        final Optional<ClassicPlotManagerComponent> componentOptional = ClassicPlotManagerComponent.fromString(component);
        if (componentOptional.isPresent()) {
            switch (componentOptional.get()) {
                case FLOOR:
                    return setFloor(plotId, blocks, queue);
                case WALL:
                    return setWallFilling(plotId, blocks, queue);
                case AIR:
                    return setAir(plotId, blocks, queue);
                case MAIN:
                    return setMain(plotId, blocks, queue);
                case MIDDLE:
                    return setMiddle(plotId, blocks, queue);
                case OUTLINE:
                    return setOutline(plotId, blocks, queue);
                case BORDER:
                    return setWall(plotId, blocks, queue);
                case ALL:
                    return setAll(plotId, blocks, queue);
            }
        }
        return false;
    }

    @Override public boolean unClaimPlot(@Nonnull Plot plot, @Nullable Runnable whenDone, @Nullable QueueCoordinator queue) {
        setWallFilling(plot.getId(), classicPlotWorld.WALL_FILLING.toPattern(), queue);
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!classicPlotWorld.WALL_BLOCK.isAir() || !classicPlotWorld.WALL_BLOCK
            .equals(classicPlotWorld.CLAIMED_WALL_BLOCK))) {
            setWall(plot.getId(), classicPlotWorld.WALL_BLOCK.toPattern(), queue);
        }
        TaskManager.runTask(whenDone);
        return true;
    }

    /**
     * Set the plot floor
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setFloor(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot != null && plot.isBasePlot()) {
            return this.regionManager
                .setCuboids(classicPlotWorld, plot.getRegions(), blocks, classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.PLOT_HEIGHT, queue);
        }
        return false;
    }

    /**
     * Sets the plot main, floor and air areas.
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setAll(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot != null && plot.isBasePlot()) {
            return this.regionManager.setCuboids(classicPlotWorld, plot.getRegions(), blocks, 1, getWorldHeight(), queue);
        }
        return false;
    }

    /**
     * Sets the plot air region.
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setAir(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot != null && plot.isBasePlot()) {
            return this.regionManager
                .setCuboids(classicPlotWorld, plot.getRegions(), blocks, classicPlotWorld.PLOT_HEIGHT + 1, getWorldHeight(), queue);
        }
        return false;
    }

    /**
     * Sets the plot main blocks.
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setMain(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot == null || plot.isBasePlot()) {
            return this.regionManager.setCuboids(classicPlotWorld, plot.getRegions(), blocks, 1, classicPlotWorld.PLOT_HEIGHT - 1, queue);
        }
        return false;
    }

    /**
     * Set the middle plot block to a Pattern
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setMiddle(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot == null || !plot.isBasePlot()) {
            return false;
        }
        Location[] corners = plot.getCorners();

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        int x = MathMan.average(corners[0].getX(), corners[1].getX());
        int z = MathMan.average(corners[0].getZ(), corners[1].getZ());
        queue.setBlock(x, classicPlotWorld.PLOT_HEIGHT, z, blocks);
        return !enqueue || queue.enqueue();
    }

    /**
     * Set a plot's outline
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setOutline(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
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
        if (plot == null) {
            return false;
        }
        Location bottom = plot.getBottomAbs();
        Location top = plot.getExtendedTopAbs();

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

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
                Location pos1 = Location.at(classicPlotWorld.getWorldName(), region.getMinimumPoint().getX(), maxY, region.getMinimumPoint().getZ());
                Location pos2 = Location.at(classicPlotWorld.getWorldName(), region.getMaximumPoint().getX(), maxY, region.getMaximumPoint().getZ());
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        return !enqueue || queue.enqueue();
    }

    /**
     * Set the wall filling for a plot
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setWallFilling(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
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
        if (plot == null) {
            return false;
        }
        Location bot = plot.getExtendedBottomAbs().subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0, plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

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
            for (int x = bot.getX(); x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                for (int y = 1; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        return !enqueue || queue.enqueue();
    }

    /**
     * Set a plot's wall top block only
     *
     * @param queue Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *              otherwise writes to the queue but does not enqueue.
     */
    public boolean setWall(@Nonnull PlotId plotId, @Nonnull Pattern blocks, @Nullable QueueCoordinator queue) {
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
        if (plot == null) {
            return false;
        }
        Location bot = plot.getExtendedBottomAbs().subtract(plot.getMerged(Direction.WEST) ? 0 : 1, 0, plot.getMerged(Direction.NORTH) ? 0 : 1);
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);

        boolean enqueue = false;
        if (queue == null) {
            enqueue = true;
            queue = classicPlotWorld.getQueue();
        }

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
            for (int x = bot.getX(); x < top.getX() + (plot.getMerged(Direction.EAST) ? 0 : 1); x++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.getMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < top.getZ() + (plot.getMerged(Direction.SOUTH) ? 0 : 1); z++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        return !enqueue || queue.enqueue();
    }

    @Override public boolean createRoadEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 2;
        int ez = pos2.getZ() + 2;

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        int maxY = getWorldHeight();
        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx, Math.min(classicPlotWorld.WALL_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, maxY, ez - 1), BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 0, sz + 1), Location.at(classicPlotWorld.getWorldName(), ex, 0, ez - 1),
            BlockUtil.get((short) 7, (byte) 0));
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT, ez - 1), classicPlotWorld.WALL_FILLING.toPattern());

        if (classicPlotWorld.PLACE_TOP_BLOCK) {
            queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT + 1, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT + 1, ez - 1), classicPlotWorld.WALL_BLOCK.toPattern());
        }
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), ex, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT, ez - 1), classicPlotWorld.WALL_FILLING.toPattern());
        if (classicPlotWorld.PLACE_TOP_BLOCK) {
            queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT + 1, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT + 1, ez - 1), classicPlotWorld.WALL_BLOCK.toPattern());
        }
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1), classicPlotWorld.ROAD_BLOCK.toPattern());
        return !enqueue || queue.enqueue();
    }

    @Override public boolean createRoadSouth(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 2;
        int ex = pos2.getX() + 2;

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        queue.setCuboid(
            Location.at(classicPlotWorld.getWorldName(), sx + 1, Math.min(classicPlotWorld.WALL_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 0, sz), Location.at(classicPlotWorld.getWorldName(), ex - 1, 0, ez),
            BlockUtil.get((short) 7, (byte) 0));
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT, sz), classicPlotWorld.WALL_FILLING.toPattern());

        if (classicPlotWorld.PLACE_TOP_BLOCK) {
            queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.WALL_HEIGHT + 1, sz),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT + 1, sz), classicPlotWorld.WALL_BLOCK.toPattern());
        }
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, ez),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT, ez), classicPlotWorld.WALL_FILLING.toPattern());
        if (classicPlotWorld.PLACE_TOP_BLOCK) {
            queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.WALL_HEIGHT + 1, ez),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT + 1, ez), classicPlotWorld.WALL_BLOCK.toPattern());
        }
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1), classicPlotWorld.ROAD_BLOCK.toPattern());
        return !enqueue || queue.enqueue();
    }


    @Override public boolean createRoadSouthEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.ROAD_HEIGHT + 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.getPlotManager().getWorldHeight(), ez - 1),
            BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 0, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, 0, ez - 1), BlockUtil.get((short) 7, (byte) 0));
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1), classicPlotWorld.ROAD_BLOCK.toPattern());
        return !enqueue || queue.enqueue();
    }

    @Override public boolean removeRoadEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sx = pos2.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = pos1.getZ() - 1;
        int ez = pos2.getZ() + 1;

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        queue
            .setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
                BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 1, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT - 1, ez - 1), classicPlotWorld.MAIN_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.PLOT_HEIGHT, sz + 1),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT, ez - 1), classicPlotWorld.TOP_BLOCK.toPattern());

        return !enqueue || queue.enqueue();
    }

    @Override public boolean removeRoadSouth(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        Location pos1 = getPlotBottomLocAbs(plot.getId());
        Location pos2 = getPlotTopLocAbs(plot.getId());
        int sz = pos2.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;
        int sx = pos1.getX() - 1;
        int ex = pos2.getX() + 1;

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        queue
            .setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
                BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.PLOT_HEIGHT - 1, ez), classicPlotWorld.MAIN_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.PLOT_HEIGHT, sz),
            Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.PLOT_HEIGHT, ez), classicPlotWorld.TOP_BLOCK.toPattern());

        return !enqueue || queue.enqueue();
    }

    @Override public boolean removeRoadSouthEast(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        Location location = getPlotTopLocAbs(plot.getId());
        int sx = location.getX() + 1;
        int ex = sx + classicPlotWorld.ROAD_WIDTH - 1;
        int sz = location.getZ() + 1;
        int ez = sz + classicPlotWorld.ROAD_WIDTH - 1;

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
        }

        queue
            .setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, Math.min(classicPlotWorld.PLOT_HEIGHT, classicPlotWorld.ROAD_HEIGHT) + 1, sz),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getPlotManager().getWorldHeight(), ez),
                BlockTypes.AIR.getDefaultState());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, 1, sz),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT - 1, ez), classicPlotWorld.MAIN_BLOCK.toPattern());
        queue.setCuboid(Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.PLOT_HEIGHT, sz),
            Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT, ez), classicPlotWorld.TOP_BLOCK.toPattern());

        return !enqueue || queue.enqueue();
    }

    @Override public boolean finishPlotMerge(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK))) {
            for (PlotId plotId : plotIds) {
                setWall(plotId, claim.toPattern(), queue);
            }
        }
        if (Settings.General.MERGE_REPLACE_WALL) {
            final BlockBucket wallBlock = classicPlotWorld.WALL_FILLING;
            for (PlotId id : plotIds) {
                setWallFilling(id, wallBlock.toPattern(), queue);
            }
        }
        return true;
    }

    @Override public boolean finishPlotUnlink(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK))) {
            for (PlotId id : plotIds) {
                setWall(id, claim.toPattern(), queue);
            }
        }
        return true; // return false if unlink has been denied
    }

    @Override public boolean startPlotMerge(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return true;
    }

    @Override public boolean startPlotUnlink(@Nonnull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return true;
    }

    @Override public boolean claimPlot(@Nonnull Plot plot, @Nullable QueueCoordinator queue) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK))) {
            return setWall(plot.getId(), claim.toPattern(), queue);
        }
        return true;
    }

    @Override public String[] getPlotComponents(@Nonnull PlotId plotId) {
        return ClassicPlotManagerComponent.stringValues();
    }

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    @Override public Location getSignLoc(@Nonnull Plot plot) {
        plot = plot.getBasePlot(false);
        final Location bot = plot.getBottomAbs();
        return Location.at(classicPlotWorld.getWorldName(), bot.getX() - 1, classicPlotWorld.ROAD_HEIGHT + 1, bot.getZ() - 2);
    }

}
