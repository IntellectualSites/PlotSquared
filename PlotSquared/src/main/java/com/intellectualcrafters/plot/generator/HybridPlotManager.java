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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class HybridPlotManager extends ClassicPlotManager {
    
    @Override
    public void exportTemplate(final PlotWorld plotworld) throws IOException {
        final HashSet<FileBytes> files = new HashSet<>(Arrays.asList(new FileBytes("templates/" + "tmp-data.yml", Template.getBytes(plotworld))));
        final String psRoot = PlotSquared.IMP.getDirectory() + File.separator;
        final String dir =  "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + plotworld.worldname + File.separator;
        final String newDir =  "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + "__TEMP_DIR__" + File.separator;
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
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        Template.zipAll(plotworld.worldname, files);
    }

    /**
     * Clearing the plot needs to only consider removing the blocks - This implementation has used the setCuboidAsync
     * function, as it is fast, and uses NMS code - It also makes use of the fact that deleting chunks is a lot faster
     * than block updates This code is very messy, but you don't need to do something quite as complex unless you happen
     * to have 512x512 sized plots
     */
    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final String world = plotworld.worldname;
        final HybridPlotWorld dpw = ((HybridPlotWorld) plotworld);
        final Location pos1 = MainUtil.getPlotBottomLocAbs(world, plot.id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLocAbs(world, plot.id);
        final PlotBlock[] plotfloor = dpw.TOP_BLOCK;
        final PlotBlock[] filling = dpw.MAIN_BLOCK;
        final PlotBlock wall;
        if (isDelete) {
            wall = dpw.WALL_BLOCK;
        } else {
            wall = dpw.CLAIMED_WALL_BLOCK;
        }
        final PlotBlock wall_filling = dpw.WALL_FILLING;
        setWallFilling(dpw, plot.id, new PlotBlock[] { wall_filling });
        final int maxy = BukkitUtil.getMaxHeight(world);
        final short bedrock = (short) (dpw.PLOT_BEDROCK ? 7 : 0);
        final int startX = (pos1.getX() / 16) * 16;
        final int startZ = (pos1.getZ() / 16) * 16;
        final int chunkX = 16 + pos2.getX();
        final int chunkZ = 16 + pos2.getZ();
        final Location l1 = MainUtil.getPlotBottomLoc(world, plot.id);
        final Location l2 = MainUtil.getPlotTopLoc(world, plot.id);
        final int plotMinX = l1.getX() + 1;
        final int plotMinZ = l1.getZ() + 1;
        final int plotMaxX = l2.getX();
        final int plotMaxZ = l2.getZ();
        Location mn = null;
        Location mx = null;
        for (int i = startX; i < chunkX; i += 16) {
            for (int j = startZ; j < chunkZ; j += 16) {
                final Plot plot1 = MainUtil.getPlot(new Location(world, i, 0, j));
                if ((plot1 != null) && (!plot1.getId().equals(plot.getId()))) {
                    break;
                }
                final Plot plot2 = MainUtil.getPlot(new Location(world, i + 15, 0, j));
                if ((plot2 != null) && (!plot2.getId().equals(plot.getId()))) {
                    break;
                }
                final Plot plot3 = MainUtil.getPlot(new Location(world, i + 15, 0, j + 15));
                if ((plot3 != null) && (!plot3.getId().equals(plot.getId()))) {
                    break;
                }
                final Plot plot4 = MainUtil.getPlot(new Location(world, i, 0, j + 15));
                if ((plot4 != null) && (!plot4.getId().equals(plot.getId()))) {
                    break;
                }
                final Plot plot5 = MainUtil.getPlot(new Location(world, i + 15, 0, j + 15));
                if ((plot5 != null) && (!plot5.getId().equals(plot.getId()))) {
                    break;
                }
                if (mn == null) {
                    mn = new Location(world, Math.max(i - 1, plotMinX), 0, Math.max(j - 1, plotMinZ));
                    mx = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                } else if ((mx.getZ() < (j + 15)) || (mx.getX() < (i + 15))) {
                    mx = new Location(world, Math.min(i + 16, plotMaxX), 0, Math.min(j + 16, plotMaxZ));
                }
                final int I = i;
                final int J = j;
                BukkitUtil.regenerateChunk(world, I / 16, J / 16);
                if (!MainUtil.canSendChunk) {
                    BukkitUtil.refreshChunk(world, I / 16, J / 16);
                }
            }
        }
        setWall(dpw, plot.id, new PlotBlock[] { wall });
        final Location max = mx;
        final Location min = mn;
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                if (min == null) {
                    MainUtil.setSimpleCuboidAsync(world, new Location(world, pos1.getX(), 0, pos1.getZ()), new Location(world, pos2.getX() + 1, 1, pos2.getZ() + 1), new PlotBlock(bedrock, (byte) 0));
                    MainUtil.setSimpleCuboidAsync(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT + 1, pos1.getZ()), new Location(world, pos2.getX() + 1, maxy + 1, pos2.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                    MainUtil.setCuboidAsync(world, new Location(world, pos1.getX(), 1, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT, pos2.getZ() + 1), filling);
                    MainUtil.setCuboidAsync(world, new Location(world, pos1.getX(), dpw.PLOT_HEIGHT, pos1.getZ()), new Location(world, pos2.getX() + 1, dpw.PLOT_HEIGHT + 1, pos2.getZ() + 1), plotfloor);
                    SetBlockQueue.addNotify(whenDone);
                    return;
                }
                if (min.getX() < plotMinX) {
                    min.setX(plotMinX);
                }
                if (min.getZ() < plotMinZ) {
                    min.setZ(plotMinZ);
                }
                if (max.getX() > plotMaxX) {
                    max.setX(plotMaxX);
                }
                if (max.getZ() > plotMaxZ) {
                    max.setZ(plotMaxZ);
                }
                MainUtil.setSimpleCuboidAsync(world, new Location(world, plotMinX, 0, plotMinZ), new Location(world, min.getX() + 1, 1, min.getZ() + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, min.getX() + 1, maxy + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, plotMinX, 1, plotMinZ), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, plotMinZ), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, min.getX(), 0, plotMinZ), new Location(world, max.getX() + 1, 1, min.getZ() + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, max.getX() + 1, maxy + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, min.getX(), 1, plotMinZ), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT, min.getZ() + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, max.getX(), 0, plotMinZ), new Location(world, plotMaxX + 1, 1, min.getZ() + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, plotMinZ), new Location(world, plotMaxX + 1, maxy + 1, min.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, max.getX(), 1, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, min.getZ() + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT, plotMinZ), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, min.getZ() + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, plotMinX, 0, min.getZ()), new Location(world, min.getX() + 1, 1, max.getZ() + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, min.getZ()), new Location(world, min.getX() + 1, maxy + 1, max.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, plotMinX, 1, min.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT, max.getZ() + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, min.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, max.getZ() + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, plotMinX, 0, max.getZ()), new Location(world, min.getX() + 1, 1, plotMaxZ + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, min.getX() + 1, maxy + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, plotMinX, 1, max.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, plotMinX, dpw.PLOT_HEIGHT, max.getZ()), new Location(world, min.getX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, min.getX(), 0, max.getZ()), new Location(world, max.getX() + 1, 1, plotMaxZ + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, max.getX() + 1, maxy + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, min.getX(), 1, max.getZ()), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, min.getX(), dpw.PLOT_HEIGHT, max.getZ()), new Location(world, max.getX() + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, max.getX(), 0, min.getZ()), new Location(world, plotMaxX + 1, 1, max.getZ() + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, min.getZ()), new Location(world, plotMaxX + 1, maxy + 1, max.getZ() + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, max.getX(), 1, min.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, max.getZ() + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT, min.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, max.getZ() + 1), plotfloor);
                MainUtil.setSimpleCuboidAsync(world, new Location(world, max.getX(), 0, max.getZ()), new Location(world, plotMaxX + 1, 1, plotMaxZ + 1), new PlotBlock(bedrock, (byte) 0));
                MainUtil.setSimpleCuboidAsync(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT + 1, max.getZ()), new Location(world, plotMaxX + 1, maxy + 1, plotMaxZ + 1), new PlotBlock((short) 0, (byte) 0));
                MainUtil.setCuboidAsync(world, new Location(world, max.getX(), 1, max.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT, plotMaxZ + 1), filling);
                MainUtil.setCuboidAsync(world, new Location(world, max.getX(), dpw.PLOT_HEIGHT, max.getZ()), new Location(world, plotMaxX + 1, dpw.PLOT_HEIGHT + 1, plotMaxZ + 1), plotfloor);
                SetBlockQueue.addNotify(whenDone);
            }
        });
        return true;
    }
}
