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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.google.common.collect.Sets;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.Template;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.FileBytes;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;

public class HybridPlotManager extends ClassicPlotManager {

    public static boolean REGENERATIVE_CLEAR = true;

    private final HybridPlotWorld hybridPlotWorld;
    private final RegionManager regionManager;
    private final ProgressSubscriberFactory subscriberFactory;

    public HybridPlotManager(@Nonnull final HybridPlotWorld hybridPlotWorld,
                             @Nonnull final RegionManager regionManager,
                             @Nonnull ProgressSubscriberFactory subscriberFactory) {
        super(hybridPlotWorld, regionManager);
        this.hybridPlotWorld = hybridPlotWorld;
        this.regionManager = regionManager;
        this.subscriberFactory = subscriberFactory;
    }

    @Override public void exportTemplate() throws IOException {
        HashSet<FileBytes> files = Sets.newHashSet(new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml", Template.getBytes(hybridPlotWorld)));
        String dir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + hybridPlotWorld.getWorldName() + File.separator;
        try {
            File sideRoad = FileUtils.getFile(PlotSquared.platform().getDirectory(), dir + "sideroad.schem");
            String newDir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + "__TEMP_DIR__" + File.separator;
            if (sideRoad.exists()) {
                files.add(new FileBytes(newDir + "sideroad.schem", Files.readAllBytes(sideRoad.toPath())));
            }
            File intersection = FileUtils.getFile(PlotSquared.platform().getDirectory(), dir + "intersection.schem");
            if (intersection.exists()) {
                files.add(new FileBytes(newDir + "intersection.schem", Files.readAllBytes(intersection.toPath())));
            }
            File plot = FileUtils.getFile(PlotSquared.platform().getDirectory(), dir + "plot.schem");
            if (plot.exists()) {
                files.add(new FileBytes(newDir + "plot.schem", Files.readAllBytes(plot.toPath())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Template.zipAll(hybridPlotWorld.getWorldName(), files);
    }

    @Override public boolean createRoadEast(@Nonnull final Plot plot, @Nullable QueueCoordinator queue) {
        super.createRoadEast(plot, queue);
        PlotId id = plot.getId();
        PlotId id2 = PlotId.of(id.getX() + 1, id.getY());
        Location bot = getPlotBottomLocAbs(id2);
        Location top = getPlotTopLocAbs(id);
        Location pos1 = Location.at(hybridPlotWorld.getWorldName(), top.getX() + 1, 0, bot.getZ() - 1);
        Location pos2 = Location.at(hybridPlotWorld.getWorldName(), bot.getX(), Math.min(getWorldHeight(), 255), top.getZ() + 1);
        this.resetBiome(hybridPlotWorld, pos1, pos2);
        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        boolean enqueue = false;
        if (queue == null) {
            queue = hybridPlotWorld.getQueue();
            enqueue = true;
        }
        createSchemAbs(queue, pos1, pos2, true);
        return !enqueue || queue.enqueue();
    }

    private void resetBiome(@Nonnull final HybridPlotWorld hybridPlotWorld, @Nonnull final Location pos1, @Nonnull final Location pos2) {
        BiomeType biome = hybridPlotWorld.getPlotBiome();
        if (!Objects.equals(PlotSquared.platform().getWorldUtil()
            .getBiomeSynchronous(hybridPlotWorld.getWorldName(), (pos1.getX() + pos2.getX()) / 2, (pos1.getZ() + pos2.getZ()) / 2), biome)) {
            WorldUtil.setBiome(hybridPlotWorld.getWorldName(), pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ(), biome);
        }
    }

    private void createSchemAbs(@Nonnull final QueueCoordinator queue, @Nonnull final Location pos1, @Nonnull final Location pos2, boolean isRoad) {
        int size = hybridPlotWorld.SIZE;
        int minY;
        if ((isRoad && Settings.Schematics.PASTE_ROAD_ON_TOP) || (!isRoad && Settings.Schematics.PASTE_ON_TOP)) {
            minY = hybridPlotWorld.SCHEM_Y;
        } else {
            minY = 1;
        }
        BaseBlock airBlock = BlockTypes.AIR.getDefaultState().toBaseBlock();
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            short absX = (short) ((x - hybridPlotWorld.ROAD_OFFSET_X) % size);
            if (absX < 0) {
                absX += size;
            }
            for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                short absZ = (short) ((z - hybridPlotWorld.ROAD_OFFSET_Z) % size);
                if (absZ < 0) {
                    absZ += size;
                }
                BaseBlock[] blocks = hybridPlotWorld.G_SCH.get(MathMan.pair(absX, absZ));
                if (blocks != null) {
                    for (int y = 0; y < blocks.length; y++) {
                        if (blocks[y] != null) {
                            queue.setBlock(x, minY + y, z, blocks[y]);
                        } else {
                            // This is necessary, otherwise any blocks not specified in the schematic will remain after a clear
                            queue.setBlock(x, minY + y, z, airBlock);
                        }
                    }
                }
                BiomeType biome = hybridPlotWorld.G_SCH_B.get(MathMan.pair(absX, absZ));
                if (biome != null) {
                    queue.setBiome(x, z, biome);
                } else {
                    queue.setBiome(x, z, hybridPlotWorld.getPlotBiome());
                }
            }
        }
    }

    @Override public boolean createRoadSouth(@Nonnull final Plot plot, @Nullable QueueCoordinator queue) {
        super.createRoadSouth(plot, queue);
        PlotId id = plot.getId();
        PlotId id2 = PlotId.of(id.getX(), id.getY() + 1);
        Location bot = getPlotBottomLocAbs(id2);
        Location top = getPlotTopLocAbs(id);
        Location pos1 = Location.at(hybridPlotWorld.getWorldName(), bot.getX() - 1, 0, top.getZ() + 1);
        Location pos2 = Location.at(hybridPlotWorld.getWorldName(), top.getX() + 1, Math.min(getWorldHeight(), 255), bot.getZ());
        this.resetBiome(hybridPlotWorld, pos1, pos2);
        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        boolean enqueue = false;
        if (queue == null) {
            enqueue = true;
            queue = hybridPlotWorld.getQueue();
        }
        createSchemAbs(queue, pos1, pos2, true);
        return !enqueue || queue.enqueue();
    }

    @Override public boolean createRoadSouthEast(@Nonnull final Plot plot, @Nullable QueueCoordinator queue) {
        super.createRoadSouthEast(plot, queue);
        PlotId id = plot.getId();
        PlotId id2 = PlotId.of(id.getX() + 1, id.getY() + 1);
        Location pos1 = getPlotTopLocAbs(id).add(1, 0, 1).withY(0);
        Location pos2 = getPlotBottomLocAbs(id2).withY(Math.min(getWorldHeight(), 255));
        boolean enqueue = false;
        if (queue == null) {
            enqueue = true;
            queue = hybridPlotWorld.getQueue();
        }
        createSchemAbs(queue, pos1, pos2, true);
        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            createSchemAbs(queue, pos1, pos2, true);
        }
        return !enqueue || queue.enqueue();
    }

    @Override public boolean clearPlot(@Nonnull final Plot plot,
                                       @Nullable final Runnable whenDone,
                                       @Nullable PlotPlayer<?> actor,
                                       @Nullable QueueCoordinator queue) {
        if (this.regionManager.notifyClear(this)) {
            //If this returns false, the clear didn't work
            if (this.regionManager.handleClear(plot, whenDone, this, actor)) {
                return true;
            }
        }
        final Location pos1 = plot.getBottomAbs();
        final Location pos2 = plot.getExtendedTopAbs();
        // If augmented
        final boolean canRegen =
            (hybridPlotWorld.getType() == PlotAreaType.AUGMENTED) && (hybridPlotWorld.getTerrain() != PlotAreaTerrainType.NONE) && REGENERATIVE_CLEAR;
        // The component blocks
        final Pattern plotfloor = hybridPlotWorld.TOP_BLOCK.toPattern();
        final Pattern filling = hybridPlotWorld.MAIN_BLOCK.toPattern();

        final BlockState bedrock;
        final BlockState air = BlockTypes.AIR.getDefaultState();
        if (hybridPlotWorld.PLOT_BEDROCK) {
            bedrock = BlockTypes.BEDROCK.getDefaultState();
        } else {
            bedrock = air;
        }

        final BiomeType biome = hybridPlotWorld.getPlotBiome();
        boolean enqueue = false;
        if (queue == null) {
            enqueue = true;
            queue = hybridPlotWorld.getQueue();
        }
        if (!canRegen) {
            queue.setCuboid(pos1.withY(0), pos2.withY(0), bedrock);
            // Each component has a different layer
            queue.setCuboid(pos1.withY(1), pos2.withY(hybridPlotWorld.PLOT_HEIGHT - 1), filling);
            queue.setCuboid(pos1.withY(hybridPlotWorld.PLOT_HEIGHT), pos2.withY(hybridPlotWorld.PLOT_HEIGHT), plotfloor);
            queue.setCuboid(pos1.withY(hybridPlotWorld.PLOT_HEIGHT + 1), pos2.withY(getWorldHeight()), air);
            queue.setBiomeCuboid(pos1, pos2, biome);
        } else {
            queue.setRegenRegion(new CuboidRegion(pos1.getBlockVector3(), pos2.getBlockVector3()));
        }
        pastePlotSchematic(queue, pos1, pos2);
        if (actor != null && Settings.QUEUE.NOTIFY_PROGRESS) {
            queue.addProgressSubscriber(subscriberFactory.createWithActor(actor));
        }
        if (whenDone != null) {
            queue.setCompleteTask(whenDone);
        }
        return !enqueue || queue.enqueue();
    }

    public void pastePlotSchematic(@Nonnull final QueueCoordinator queue, @Nonnull final Location bottom, @Nonnull final Location top) {
        if (!hybridPlotWorld.PLOT_SCHEMATIC) {
            return;
        }
        createSchemAbs(queue, bottom, top, false);
    }

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    @Override public Location getSignLoc(@Nonnull final Plot plot) {
        return hybridPlotWorld.getSignLocation(plot);
    }

    public HybridPlotWorld getHybridPlotWorld() {
        return this.hybridPlotWorld;
    }
}
