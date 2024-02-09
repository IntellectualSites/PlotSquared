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

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A plot manager with square plots which tessellate on a square grid with the following sections: ROAD, WALL, BORDER (wall), PLOT, FLOOR (plot).
 */
public class ClassicPlotManager extends SquarePlotManager {

    private final ClassicPlotWorld classicPlotWorld;
    private final RegionManager regionManager;
    private final ProgressSubscriberFactory subscriberFactory;

    @Inject
    public ClassicPlotManager(final @NonNull ClassicPlotWorld classicPlotWorld, final @NonNull RegionManager regionManager) {
        super(classicPlotWorld, regionManager);
        this.classicPlotWorld = classicPlotWorld;
        this.regionManager = regionManager;
        this.subscriberFactory = PlotSquared.platform().injector().getInstance(ProgressSubscriberFactory.class);
    }

    @Override
    public boolean setComponent(
            @NonNull PlotId plotId,
            @NonNull String component,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        final Optional<ClassicPlotManagerComponent> componentOptional = ClassicPlotManagerComponent.fromString(component);
        return componentOptional.map(classicPlotManagerComponent -> switch (classicPlotManagerComponent) {
            case FLOOR -> setFloor(plotId, blocks, actor, queue);
            case WALL -> setWallFilling(plotId, blocks, actor, queue);
            case AIR -> setAir(plotId, blocks, actor, queue);
            case MAIN -> setMain(plotId, blocks, actor, queue);
            case MIDDLE -> setMiddle(plotId, blocks, queue);
            case OUTLINE -> setOutline(plotId, blocks, actor, queue);
            case BORDER -> setWall(plotId, blocks, actor, queue);
            case ALL -> setAll(plotId, blocks, actor, queue);
        }).orElse(false);
    }

    @Override
    public boolean unClaimPlot(@NonNull Plot plot, @Nullable Runnable whenDone, @Nullable QueueCoordinator queue) {
        setWallFilling(plot.getId(), classicPlotWorld.WALL_FILLING.toPattern(), null, queue);
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!classicPlotWorld.WALL_BLOCK.isAir() || !classicPlotWorld.WALL_BLOCK
                .equals(classicPlotWorld.CLAIMED_WALL_BLOCK))) {
            setWall(plot.getId(), classicPlotWorld.WALL_BLOCK.toPattern(), null, queue);
        }
        TaskManager.runTask(whenDone);
        return true;
    }

    /**
     * Set the plot floor
     *
     * @param plotId id of plot to set floor of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setFloor(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot != null && plot.isBasePlot()) {
            return this.regionManager
                    .setCuboids(
                            classicPlotWorld,
                            plot.getRegions(),
                            blocks,
                            classicPlotWorld.PLOT_HEIGHT,
                            classicPlotWorld.PLOT_HEIGHT,
                            actor,
                            queue
                    );
        }
        return false;
    }

    /**
     * Sets the plot main, floor and air areas.
     *
     * @param plotId id of plot to set all of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setAll(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot != null && plot.isBasePlot()) {
            return this.regionManager.setCuboids(
                    classicPlotWorld,
                    plot.getRegions(),
                    blocks,
                    classicPlotWorld.getMinComponentHeight(),
                    classicPlotWorld.getMaxBuildHeight() - 1,
                    actor,
                    queue
            );
        }
        return false;
    }

    /**
     * Sets the plot air region.
     *
     * @param plotId id of plot to set air of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setAir(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot != null && plot.isBasePlot()) {
            return this.regionManager
                    .setCuboids(
                            classicPlotWorld,
                            plot.getRegions(),
                            blocks,
                            classicPlotWorld.PLOT_HEIGHT + 1,
                            classicPlotWorld.getMaxBuildHeight() - 1,
                            actor,
                            queue
                    );
        }
        return false;
    }

    /**
     * Sets the plot main blocks.
     *
     * @param plotId id of plot to set main of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setMain(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
        Plot plot = classicPlotWorld.getPlotAbs(plotId);
        if (plot == null || plot.isBasePlot()) {
            return this.regionManager.setCuboids(
                    classicPlotWorld,
                    plot.getRegions(),
                    blocks,
                    classicPlotWorld.getMinComponentHeight(),
                    classicPlotWorld.PLOT_HEIGHT - 1,
                    actor,
                    queue
            );
        }
        return false;
    }

    /**
     * Set the middle plot block to a Pattern
     *
     * @param plotId id of plot to set middle block of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setMiddle(@NonNull PlotId plotId, @NonNull Pattern blocks, @Nullable QueueCoordinator queue) {
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
     * @param plotId id of plot to set outline of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setOutline(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
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
            if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
                queue.addProgressSubscriber(subscriberFactory.createWithActor(actor));
            }
        }

        int maxY = classicPlotWorld.getMaxBuildHeight() - 1;
        if (!plot.isMerged(Direction.NORTH)) {
            int z = bottom.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.isMerged(Direction.WEST)) {
            int x = bottom.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }

        if (!plot.isMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bottom.getX(); x <= top.getX(); x++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.isMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bottom.getZ(); z <= top.getZ(); z++) {
                for (int y = classicPlotWorld.PLOT_HEIGHT; y <= maxY; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (plot.isBasePlot()) {
            for (CuboidRegion region : plot.getRegions()) {
                Location pos1 = Location.at(
                        classicPlotWorld.getWorldName(),
                        region.getMinimumPoint().getX(),
                        maxY,
                        region.getMinimumPoint().getZ()
                );
                Location pos2 = Location.at(
                        classicPlotWorld.getWorldName(),
                        region.getMaximumPoint().getX(),
                        maxY,
                        region.getMaximumPoint().getZ()
                );
                queue.setCuboid(pos1, pos2, blocks);
            }
        }
        return !enqueue || queue.enqueue();
    }

    /**
     * Set the wall filling for a plot
     *
     * @param plotId id of plot to set wall filling of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setWallFilling(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
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
        Location bot = plot.getExtendedBottomAbs().subtract(
                plot.isMerged(Direction.WEST) ? 0 : 1,
                0,
                plot.isMerged(Direction.NORTH) ? 0 : 1
        );
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);

        boolean enqueue = false;
        if (queue == null) {
            queue = classicPlotWorld.getQueue();
            enqueue = true;
            if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
                queue.addProgressSubscriber(subscriberFactory.createWithActor(actor));
            }
        }

        int yStart = classicPlotWorld.getMinComponentHeight();
        if (!plot.isMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                for (int y = yStart; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.isMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                for (int y = yStart; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.isMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX(); x < top.getX() + (plot.isMerged(Direction.EAST) ? 0 : 1); x++) {
                for (int y = yStart; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        if (!plot.isMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < top.getZ() + (plot.isMerged(Direction.SOUTH) ? 0 : 1); z++) {
                for (int y = yStart; y <= classicPlotWorld.WALL_HEIGHT; y++) {
                    queue.setBlock(x, y, z, blocks);
                }
            }
        }
        return !enqueue || queue.enqueue();
    }

    /**
     * Set a plot's wall top block only
     *
     * @param plotId id of plot to set wall top block of
     * @param blocks pattern to set
     * @param queue  Nullable {@link QueueCoordinator}. If null, creates own queue and enqueues,
     *               otherwise writes to the queue but does not enqueue.
     * @return success or not
     */
    public boolean setWall(
            @NonNull PlotId plotId,
            @NonNull Pattern blocks,
            @Nullable PlotPlayer<?> actor,
            @Nullable QueueCoordinator queue
    ) {
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
        Location bot = plot.getExtendedBottomAbs().subtract(
                plot.isMerged(Direction.WEST) ? 0 : 1,
                0,
                plot.isMerged(Direction.NORTH) ? 0 : 1
        );
        Location top = plot.getExtendedTopAbs().add(1, 0, 1);

        boolean enqueue = false;
        if (queue == null) {
            enqueue = true;
            queue = classicPlotWorld.getQueue();
            if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
                queue.addProgressSubscriber(subscriberFactory.createWithActor(actor));
            }
        }

        int y = classicPlotWorld.WALL_HEIGHT + 1;
        if (!plot.isMerged(Direction.NORTH)) {
            int z = bot.getZ();
            for (int x = bot.getX(); x < top.getX(); x++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.isMerged(Direction.WEST)) {
            int x = bot.getX();
            for (int z = bot.getZ(); z < top.getZ(); z++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.isMerged(Direction.SOUTH)) {
            int z = top.getZ();
            for (int x = bot.getX(); x < top.getX() + (plot.isMerged(Direction.EAST) ? 0 : 1); x++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        if (!plot.isMerged(Direction.EAST)) {
            int x = top.getX();
            for (int z = bot.getZ(); z < top.getZ() + (plot.isMerged(Direction.SOUTH) ? 0 : 1); z++) {
                queue.setBlock(x, y, z, blocks);
            }
        }
        return !enqueue || queue.enqueue();
    }

    @Override
    public boolean createRoadEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
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

        int maxY = classicPlotWorld.getMaxGenHeight();
        queue.setCuboid(
                Location.at(
                        classicPlotWorld.getWorldName(),
                        sx,
                        classicPlotWorld.schematicStartHeight() + 1,
                        sz + 1
                ),
                Location.at(classicPlotWorld.getWorldName(), ex, maxY, ez - 1), BlockTypes.AIR.getDefaultState()
        );
        if (classicPlotWorld.PLOT_BEDROCK) {
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.getMinGenHeight(), sz + 1),
                    Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getMinGenHeight(), ez - 1),
                    BlockTypes.BEDROCK.getDefaultState()
            );
        }
        int startYOffset = classicPlotWorld.PLOT_BEDROCK ? 1 : 0;
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getMinGenHeight() + startYOffset, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT, ez - 1),
                classicPlotWorld.WALL_FILLING.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.getMinGenHeight() + startYOffset, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT, ez - 1),
                classicPlotWorld.WALL_FILLING.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight() + startYOffset, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1),
                classicPlotWorld.ROAD_BLOCK.toPattern()
        );

        if (classicPlotWorld.PLACE_TOP_BLOCK) {
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT + 1, sz + 1),
                    Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.WALL_HEIGHT + 1, ez - 1),
                    classicPlotWorld.WALL_BLOCK.toPattern()
            );
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT + 1, sz + 1),
                    Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.WALL_HEIGHT + 1, ez - 1),
                    classicPlotWorld.WALL_BLOCK.toPattern()
            );
        }
        return !enqueue || queue.enqueue();
    }

    @Override
    public boolean createRoadSouth(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
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
                Location.at(
                        classicPlotWorld.getWorldName(),
                        sx + 1,
                        classicPlotWorld.schematicStartHeight() + 1,
                        sz
                ),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.getMaxGenHeight(), ez),
                BlockTypes.AIR.getDefaultState()
        );
        if (classicPlotWorld.PLOT_BEDROCK) {
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight(), sz),
                    Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.getMinGenHeight(), ez),
                    BlockTypes.BEDROCK.getDefaultState()
            );
        }
        int startYOffset = classicPlotWorld.PLOT_BEDROCK ? 1 : 0;
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight() + startYOffset, sz),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT, sz),
                classicPlotWorld.WALL_FILLING.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight() + startYOffset, ez),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT, ez),
                classicPlotWorld.WALL_FILLING.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight() + startYOffset, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1),
                classicPlotWorld.ROAD_BLOCK.toPattern()
        );

        if (classicPlotWorld.PLACE_TOP_BLOCK) {
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.WALL_HEIGHT + 1, sz),
                    Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT + 1, sz),
                    classicPlotWorld.WALL_BLOCK.toPattern()
            );
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.WALL_HEIGHT + 1, ez),
                    Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.WALL_HEIGHT + 1, ez),
                    classicPlotWorld.WALL_BLOCK.toPattern()
            );
        }
        return !enqueue || queue.enqueue();
    }


    @Override
    public boolean createRoadSouthEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
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

        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.ROAD_HEIGHT + 1, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.getMaxGenHeight(), ez - 1),
                BlockTypes.AIR.getDefaultState()
        );
        if (classicPlotWorld.PLOT_BEDROCK) {
            queue.setCuboid(
                    Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight(), sz + 1),
                    Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.getMinGenHeight(), ez - 1),
                    BlockTypes.BEDROCK.getDefaultState()
            );
        }
        int startYOffset = classicPlotWorld.PLOT_BEDROCK ? 1 : 0;
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight() + startYOffset, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.ROAD_HEIGHT, ez - 1),
                classicPlotWorld.ROAD_BLOCK.toPattern()
        );
        return !enqueue || queue.enqueue();
    }

    @Override
    public boolean removeRoadEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
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
                .setCuboid(
                        Location.at(
                                classicPlotWorld.getWorldName(),
                                sx,
                                classicPlotWorld.schematicStartHeight() + 1,
                                sz
                        ),
                        Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getMaxGenHeight(), ez),
                        BlockTypes.AIR.getDefaultState()
                );
        int startYOffset = classicPlotWorld.PLOT_BEDROCK ? 1 : 0;
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.getMinGenHeight() + startYOffset, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT - 1, ez - 1),
                classicPlotWorld.MAIN_BLOCK.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.PLOT_HEIGHT, sz + 1),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT, ez - 1),
                classicPlotWorld.TOP_BLOCK.toPattern()
        );

        return !enqueue || queue.enqueue();
    }

    @Override
    public boolean removeRoadSouth(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
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
                .setCuboid(
                        Location.at(
                                classicPlotWorld.getWorldName(),
                                sx,
                                classicPlotWorld.schematicStartHeight() + 1,
                                sz
                        ),
                        Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getMaxGenHeight(), ez),
                        BlockTypes.AIR.getDefaultState()
                );
        int startYOffset = classicPlotWorld.PLOT_BEDROCK ? 1 : 0;
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.getMinGenHeight() + startYOffset, sz),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.PLOT_HEIGHT - 1, ez),
                classicPlotWorld.MAIN_BLOCK.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx + 1, classicPlotWorld.PLOT_HEIGHT, sz),
                Location.at(classicPlotWorld.getWorldName(), ex - 1, classicPlotWorld.PLOT_HEIGHT, ez),
                classicPlotWorld.TOP_BLOCK.toPattern()
        );

        return !enqueue || queue.enqueue();
    }

    @Override
    public boolean removeRoadSouthEast(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
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
                .setCuboid(
                        Location.at(
                                classicPlotWorld.getWorldName(),
                                sx,
                                classicPlotWorld.schematicStartHeight() + 1,
                                sz
                        ),
                        Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.getMaxGenHeight(), ez),
                        BlockTypes.AIR.getDefaultState()
                );
        int startYOffset = classicPlotWorld.PLOT_BEDROCK ? 1 : 0;
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.getMinGenHeight() + startYOffset, sz),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT - 1, ez),
                classicPlotWorld.MAIN_BLOCK.toPattern()
        );
        queue.setCuboid(
                Location.at(classicPlotWorld.getWorldName(), sx, classicPlotWorld.PLOT_HEIGHT, sz),
                Location.at(classicPlotWorld.getWorldName(), ex, classicPlotWorld.PLOT_HEIGHT, ez),
                classicPlotWorld.TOP_BLOCK.toPattern()
        );

        return !enqueue || queue.enqueue();
    }

    @Override
    public boolean finishPlotMerge(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK))) {
            for (PlotId plotId : plotIds) {
                setWall(plotId, claim.toPattern(), null, queue);
            }
        }
        if (Settings.General.MERGE_REPLACE_WALL) {
            final BlockBucket wallBlock = classicPlotWorld.WALL_FILLING;
            for (PlotId id : plotIds) {
                setWallFilling(id, wallBlock.toPattern(), null, queue);
            }
        }
        return true;
    }

    @Override
    public boolean finishPlotUnlink(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK))) {
            for (PlotId id : plotIds) {
                setWall(id, claim.toPattern(), null, queue);
            }
        }
        return true; // return false if unlink has been denied
    }

    @Override
    public boolean startPlotMerge(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return true;
    }

    @Override
    public boolean startPlotUnlink(@NonNull List<PlotId> plotIds, @Nullable QueueCoordinator queue) {
        return true;
    }

    @Override
    public boolean claimPlot(@NonNull Plot plot, @Nullable QueueCoordinator queue) {
        final BlockBucket claim = classicPlotWorld.CLAIMED_WALL_BLOCK;
        if (classicPlotWorld.PLACE_TOP_BLOCK && (!claim.isAir() || !claim.equals(classicPlotWorld.WALL_BLOCK))) {
            return setWall(plot.getId(), claim.toPattern(), null, queue);
        }
        return true;
    }

    @Override
    public String[] getPlotComponents(@NonNull PlotId plotId) {
        return ClassicPlotManagerComponent.stringValues();
    }

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    @Override
    public Location getSignLoc(@NonNull Plot plot) {
        plot = plot.getBasePlot(false);
        final Location bot = plot.getBottomAbs();
        return Location.at(classicPlotWorld.getWorldName(), bot.getX() - 1, classicPlotWorld.ROAD_HEIGHT + 1, bot.getZ() - 2);
    }

}
