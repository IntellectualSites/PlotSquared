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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class HybridPlotManager extends ClassicPlotManager {
    
    @Override
    public void exportTemplate(final PlotArea plotworld) throws IOException {
        final HashSet<FileBytes> files = new HashSet<>(
                Collections.singletonList(new FileBytes("templates/" + "tmp-data.yml", Template.getBytes(plotworld))));
        final String psRoot = PS.get().IMP.getDirectory() + File.separator;
        final String dir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plotworld.worldname + File.separator;
        final String newDir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + "__TEMP_DIR__" + File.separator;
        try {
            final File sideroad = new File(psRoot + dir + "sideroad.schematic");
            if (sideroad.exists()) {
                files.add(new FileBytes(newDir + "sideroad.schematic", Files.readAllBytes(sideroad.toPath())));
            }
            final File intersection = new File(psRoot + dir + "intersection.schematic");
            if (intersection.exists()) {
                files.add(new FileBytes(newDir + "intersection.schematic", Files.readAllBytes(intersection.toPath())));
            }
            final File plot = new File(psRoot + dir + "plot.schematic");
            if (plot.exists()) {
                files.add(new FileBytes(newDir + "plot.schematic", Files.readAllBytes(plot.toPath())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Template.zipAll(plotworld.worldname, files);
    }
    
    @Override
    public boolean createRoadEast(final PlotArea plotworld, final Plot plot) {
        super.createRoadEast(plotworld, plot);
        final HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        final PlotId id = plot.getId();
        final PlotId id2 = new PlotId(id.x + 1, id.y);
        final Location bot = getPlotBottomLocAbs(hpw, id2);
        final Location top = getPlotTopLocAbs(hpw, id);
        final Location pos1 = new Location(plotworld.worldname, top.getX() + 1, 0, bot.getZ() - 1);
        final Location pos2 = new Location(plotworld.worldname, bot.getX(), 255, top.getZ() + 1);
        MainUtil.resetBiome(plotworld, pos1, pos2);
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        createSchemAbs(hpw, pos1, pos2, 0, true);
        return true;
    }

    private void createSchemAbs(final HybridPlotWorld hpw, final Location pos1, final Location pos2, final int height, final boolean clear) {
        final int size = hpw.SIZE;
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            short absX = (short) ((x - hpw.ROAD_OFFSET_X) % (size));
            if (absX < 0) {
                absX += size;
            }
            for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                short absZ = (short) ((z - hpw.ROAD_OFFSET_Z) % (size));
                if (absZ < 0) {
                    absZ += size;
                }
                final HashMap<Integer, PlotBlock> blocks = hpw.G_SCH.get(MathMan.pair(absX, absZ));
                if (clear) {
                    for (short y = (short) (height); y <= (height + hpw.SCHEMATIC_HEIGHT); y++) {
                        SetQueue.IMP.setBlock(hpw.worldname, x, y, z, 0);
                    }
                }
                if (blocks != null) {
                    for (Entry<Integer, PlotBlock> entry : blocks.entrySet()) {
                        SetQueue.IMP.setBlock(hpw.worldname, x, height + entry.getKey(), z, entry.getValue());
                    }
                }
            }
        }
    }
    
    @Override
    public boolean createRoadSouth(final PlotArea plotworld, final Plot plot) {
        super.createRoadSouth(plotworld, plot);
        final HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        final PlotId id = plot.getId();
        final PlotId id2 = new PlotId(id.x, id.y + 1);
        final Location bot = getPlotBottomLocAbs(hpw, id2);
        final Location top = getPlotTopLocAbs(hpw, id);
        final Location pos1 = new Location(plotworld.worldname, bot.getX() - 1, 0, top.getZ() + 1);
        final Location pos2 = new Location(plotworld.worldname, top.getX() + 1, 255, bot.getZ());
        MainUtil.resetBiome(plotworld, pos1, pos2);
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        createSchemAbs(hpw, pos1, pos2, 0, true);
        return true;
    }
    
    @Override
    public boolean createRoadSouthEast(final PlotArea plotworld, final Plot plot) {
        super.createRoadSouthEast(plotworld, plot);
        final HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        final PlotId id = plot.getId();
        final PlotId id2 = new PlotId(id.x + 1, id.y + 1);
        final Location pos1 = getPlotTopLocAbs(hpw, id).add(1, 0, 1);
        final Location pos2 = getPlotBottomLocAbs(hpw, id2);
        pos1.setY(0);
        pos2.setY(256);
        createSchemAbs(hpw, pos1, pos2, 0, true);
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        createSchemAbs(hpw, pos1, pos2, 0, true);
        return true;
    }
    
    /**
     * Clearing the plot needs to only consider removing the blocks - This implementation has used the setCuboidAsync
     * function, as it is fast, and uses NMS code - It also makes use of the fact that deleting chunks is a lot faster
     * than block updates This code is very messy, but you don't need to do something quite as complex unless you happen
     * to have 512x512 sized plots
     */
    @Override
    public boolean clearPlot(final PlotArea plotworld, final Plot plot, final Runnable whenDone) {
        final String world = plotworld.worldname;
        final HybridPlotWorld dpw = ((HybridPlotWorld) plotworld);
        final Location pos1 = plot.getBottomAbs();
        final Location pos2 = plot.getExtendedTopAbs();
        // If augmented
        final boolean canRegen = (plotworld.TYPE == 0) && (plotworld.TERRAIN == 0);
        // The component blocks
        final PlotBlock[] plotfloor = dpw.TOP_BLOCK;
        final PlotBlock[] filling = dpw.MAIN_BLOCK;
        final PlotBlock bedrock = (dpw.PLOT_BEDROCK ? new PlotBlock((short) 7, (byte) 0) : new PlotBlock((short) 0, (byte) 0));
        final PlotBlock air = new PlotBlock((short) 0, (byte) 0);
        final String biome = WorldUtil.IMP.getBiomeList()[dpw.PLOT_BIOME];
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override
            public void run(int[] value) {
                // If the chunk isn't near the edge and it isn't an augmented world we can just regen the whole chunk
                if (canRegen && (value[6] == 0)) {
                    ChunkManager.manager.regenerateChunk(world, new ChunkLoc(value[0], value[1]));
                    return;
                }
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // Otherwise we need to set each component, as we don't want to regenerate the road or other plots that share the same chunk //
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // Set the biome
                MainUtil.setBiome(world, value[2], value[3], value[4], value[5], biome);
                // These two locations are for each component (e.g. bedrock, main block, floor, air)
                final Location bot = new Location(world, value[2], 0, value[3]);
                final Location top = new Location(world, value[4], 1, value[5]);
                MainUtil.setSimpleCuboidAsync(world, bot, top, bedrock);
                // Each component has a different layer
                bot.setY(1);
                top.setY(dpw.PLOT_HEIGHT);
                MainUtil.setCuboidAsync(world, bot, top, filling);
                bot.setY(dpw.PLOT_HEIGHT);
                top.setY(dpw.PLOT_HEIGHT + 1);
                MainUtil.setCuboidAsync(world, bot, top, plotfloor);
                bot.setY(dpw.PLOT_HEIGHT + 1);
                top.setY(256);
                MainUtil.setSimpleCuboidAsync(world, bot, top, air);
                // And finally set the schematic, the y value is unimportant for this function
                pastePlotSchematic(dpw, bot, top);
            }
        }, new Runnable() {
            @Override
            public void run() {
                // And notify whatever called this when plot clearing is done
                SetQueue.IMP.addTask(whenDone);
            }
        }, 10);
        return true;
    }
    
    public void pastePlotSchematic(final HybridPlotWorld plotworld, final Location l1, final Location l2) {
        if (!plotworld.PLOT_SCHEMATIC) {
            return;
        }
        createSchemAbs(plotworld, l1, l2, 0, false);
    }
}
