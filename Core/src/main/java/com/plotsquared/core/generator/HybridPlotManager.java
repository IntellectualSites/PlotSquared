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

import com.google.common.collect.Sets;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.Template;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.FileBytes;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

public class HybridPlotManager extends ClassicPlotManager {

    public static boolean REGENERATIVE_CLEAR = true;

    private final HybridPlotWorld hybridPlotWorld;
    private final RegionManager regionManager;

    public HybridPlotManager(@Nonnull final HybridPlotWorld hybridPlotWorld,
                             @Nonnull final RegionManager regionManager) {
        super(hybridPlotWorld, regionManager);
        this.hybridPlotWorld = hybridPlotWorld;
        this.regionManager = regionManager;
    }

    @Override public void exportTemplate() throws IOException {
        HashSet<FileBytes> files = Sets.newHashSet(
            new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml",
                Template.getBytes(hybridPlotWorld)));
        String dir =
            "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + hybridPlotWorld
                .getWorldName() + File.separator;
        try {
            File sideRoad =
                MainUtil.getFile(PlotSquared.platform().getDirectory(), dir + "sideroad.schem");
            String newDir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator
                + "__TEMP_DIR__" + File.separator;
            if (sideRoad.exists()) {
                files.add(new FileBytes(newDir + "sideroad.schem",
                    Files.readAllBytes(sideRoad.toPath())));
            }
            File intersection =
                MainUtil.getFile(PlotSquared.platform().getDirectory(), dir + "intersection.schem");
            if (intersection.exists()) {
                files.add(new FileBytes(newDir + "intersection.schem",
                    Files.readAllBytes(intersection.toPath())));
            }
            File plot = MainUtil.getFile(PlotSquared.platform().getDirectory(), dir + "plot.schem");
            if (plot.exists()) {
                files.add(new FileBytes(newDir + "plot.schem", Files.readAllBytes(plot.toPath())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Template.zipAll(hybridPlotWorld.getWorldName(), files);
    }

    @Override public boolean createRoadEast(Plot plot) {
        super.createRoadEast(plot);
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x + 1, id.y);
        Location bot = getPlotBottomLocAbs(id2);
        Location top = getPlotTopLocAbs(id);
        Location pos1 = Location.at(hybridPlotWorld.getWorldName(), top.getX() + 1, 0, bot.getZ() - 1);
        Location pos2 = Location.at(hybridPlotWorld.getWorldName(), bot.getX(),
            Math.min(getWorldHeight(), 255), top.getZ() + 1);
        MainUtil.resetBiome(hybridPlotWorld, pos1, pos2);
        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        createSchemAbs(queue, pos1, pos2, true);
        queue.enqueue();
        return true;
    }

    private void createSchemAbs(LocalBlockQueue queue, Location pos1, Location pos2,
        boolean isRoad) {
        int size = hybridPlotWorld.SIZE;
        int minY;
        if ((isRoad && Settings.Schematics.PASTE_ROAD_ON_TOP) || (!isRoad
            && Settings.Schematics.PASTE_ON_TOP)) {
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

    @Override public boolean createRoadSouth(Plot plot) {
        super.createRoadSouth(plot);
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x, id.y + 1);
        Location bot = getPlotBottomLocAbs(id2);
        Location top = getPlotTopLocAbs(id);
        Location pos1 = Location.at(hybridPlotWorld.getWorldName(), bot.getX() - 1, 0, top.getZ() + 1);
        Location pos2 = Location.at(hybridPlotWorld.getWorldName(), top.getX() + 1,
            Math.min(getWorldHeight(), 255), bot.getZ());
        MainUtil.resetBiome(hybridPlotWorld, pos1, pos2);
        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        createSchemAbs(queue, pos1, pos2, true);
        queue.enqueue();
        return true;
    }

    @Override public boolean createRoadSouthEast(Plot plot) {
        super.createRoadSouthEast(plot);
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x + 1, id.y + 1);
        Location pos1 = getPlotTopLocAbs(id).add(1, 0, 1).withY(0);
        Location pos2 = getPlotBottomLocAbs(id2).withY(Math.min(getWorldHeight(), 255));
        LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        createSchemAbs(queue, pos1, pos2, true);
        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            createSchemAbs(queue, pos1, pos2, true);
        }
        return queue.enqueue();
    }

    /**
     * <p>Clearing the plot needs to only consider removing the blocks - This implementation has
     * used the setCuboidAsync function, as it is fast, and uses NMS code - It also makes use of the
     * fact that deleting chunks is a lot faster than block updates This code is very messy, but you
     * don't need to do something quite as complex unless you happen to have 512x512 sized plots.
     * </p>
     */
    @Override public boolean clearPlot(Plot plot, final Runnable whenDone) {
        if (this.regionManager.notifyClear(this)) {
            //If this returns false, the clear didn't work
            if (this.regionManager.handleClear(plot, whenDone, this)) {
                return true;
            }
        }
        final String world = hybridPlotWorld.getWorldName();
        Location pos1 = plot.getBottomAbs();
        Location pos2 = plot.getExtendedTopAbs();
        // If augmented
        final boolean canRegen =
            (hybridPlotWorld.getType() == PlotAreaType.AUGMENTED) && (hybridPlotWorld.getTerrain()
                != PlotAreaTerrainType.NONE) && REGENERATIVE_CLEAR;
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
        final LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                // If the chunk isn't near the edge and it isn't an augmented world we can just regen the whole chunk
                if (canRegen && (value[6] == 0)) {
                    queue.regenChunk(value[0], value[1]);
                    return;
                }
                /* Otherwise we need to set each component, as we don't want to regenerate the road or other plots that share the same chunk.*/
                // Set the biome
                MainUtil.setBiome(world, value[2], value[3], value[4], value[5], biome);
                // These two locations are for each component (e.g. bedrock, main block, floor, air)
                Location bot = Location.at(world, value[2], 0, value[3]);
                Location top = Location.at(world, value[4], 1, value[5]);
                queue.setCuboid(bot, top, bedrock);
                // Each component has a different layer
                bot = bot.withY(1);
                top = top.withY(hybridPlotWorld.PLOT_HEIGHT);
                queue.setCuboid(bot, top, filling);
                bot = bot.withY(hybridPlotWorld.PLOT_HEIGHT);
                top = top.withY(hybridPlotWorld.PLOT_HEIGHT + 1);
                queue.setCuboid(bot, top, plotfloor);
                bot = bot.withY(hybridPlotWorld.PLOT_HEIGHT + 1);
                top = top.withY(getWorldHeight());
                queue.setCuboid(bot, top, air);
                // And finally set the schematic, the y value is unimportant for this function
                pastePlotSchematic(queue, bot, top);
            }
        }, () -> {
            queue.enqueue();
            // And notify whatever called this when plot clearing is done
            PlotSquared.platform().getGlobalBlockQueue().addEmptyTask(whenDone);
        }, 10);
        return true;
    }

    public void pastePlotSchematic(LocalBlockQueue queue, Location bottom, Location top) {
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
    @Override public Location getSignLoc(Plot plot) {
        return hybridPlotWorld.getSignLocation(plot);
    }

    public HybridPlotWorld getHybridPlotWorld() {
        return this.hybridPlotWorld;
    }
}
