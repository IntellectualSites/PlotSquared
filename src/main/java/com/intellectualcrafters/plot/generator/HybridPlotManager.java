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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotLoc;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class HybridPlotManager extends ClassicPlotManager {
    
    @Override
    public void exportTemplate(final PlotWorld plotworld) throws IOException {
        final HashSet<FileBytes> files = new HashSet<>(Arrays.asList(new FileBytes("templates/" + "tmp-data.yml", Template.getBytes(plotworld))));
        final String psRoot = PS.get().IMP.getDirectory() + File.separator;
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

    @Override
    public boolean createRoadEast(PlotWorld plotworld, Plot plot) {
        super.createRoadEast(plotworld, plot);
        HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        PlotId id = plot.id;
        PlotId id2 = new PlotId(id.x + 1, id.y);
        Location bot = getPlotBottomLocAbs(hpw, id2);
        Location top = getPlotTopLocAbs(hpw, id);
        Location pos1 = new Location(plot.world, top.getX() + 1, 0, bot.getZ());
        Location pos2 = new Location(plot.world, bot.getX(), 256, top.getZ() + 1);
        createSchemAbs(hpw, pos1, pos2, hpw.ROAD_HEIGHT, true);
        return true;
    }
    
    public void createSchemAbs(HybridPlotWorld hpw, Location pos1, Location pos2, int height, boolean clear) {
        final int size = hpw.SIZE;
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                short absX = (short) (x % size);
                short absZ = (short) (z % size);
                if (absX < 0) {
                    absX += size;
                }
                if (absZ < 0) {
                    absZ += size;
                }
                final PlotLoc loc = new PlotLoc(absX, absZ);
                final HashMap<Short, Short> blocks = hpw.G_SCH.get(loc);
                if (clear) {
                    for (short y = (short) (height); y <= (height + hpw.SCHEMATIC_HEIGHT); y++) {
                        SetBlockQueue.setBlock(hpw.worldname, x, y + y, z, 0);
                    }
                }
                if (blocks != null) {
                    final HashMap<Short, Byte> datas = hpw.G_SCH_DATA.get(loc);
                    if (datas == null) {
                        for (final Short y : blocks.keySet()) {
                            SetBlockQueue.setBlock(hpw.worldname, x, height + y, z, blocks.get(y));
                        }
                    } else {
                        for (final Short y : blocks.keySet()) {
                            Byte data = datas.get(y);
                            if (data == null) {
                                data = 0;
                            }
                            SetBlockQueue.setBlock(hpw.worldname, x, height + y, z, new PlotBlock(blocks.get(y), data));
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public boolean createRoadSouth(PlotWorld plotworld, Plot plot) {
        super.createRoadSouth(plotworld, plot);
        HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        PlotId id = plot.id;
        PlotId id2 = new PlotId(id.x, id.y + 1);
        Location bot = getPlotBottomLocAbs(hpw, id2);
        Location top = getPlotTopLocAbs(hpw, id);
        Location pos1 = new Location(plot.world, bot.getX(), 0, top.getZ() + 1);
        Location pos2 = new Location(plot.world, top.getX() + 1, 256, bot.getZ());
        createSchemAbs(hpw, pos1, pos2, hpw.ROAD_HEIGHT, true);
        return true;
    }
    
    @Override
    public boolean createRoadSouthEast(PlotWorld plotworld, Plot plot) {
        super.createRoadSouthEast(plotworld, plot);
        HybridPlotWorld hpw = (HybridPlotWorld) plotworld;
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        PlotId id = plot.id;
        PlotId id2 = new PlotId(id.x + 1, id.y + 1);
        Location pos1 = getPlotTopLocAbs(hpw, id).add(1, 0, 1);
        Location pos2 = getPlotBottomLocAbs(hpw, id2);
        pos1.setY(0);
        pos2.setY(256);
        createSchemAbs(hpw, pos1, pos2, hpw.ROAD_HEIGHT, true);
        return true;
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
        
        setWallFilling(dpw, plot.id, new PlotBlock[] { dpw.WALL_FILLING });
        final int p1x = pos1.getX();
        final int p1z = pos1.getZ();
        final int p2x = pos2.getX();
        final int p2z = pos2.getZ();
        final int bcx = p1x >> 4;
        final int bcz = p1z >> 4;
        final int tcx = p2x >> 4;
        final int tcz = p2z >> 4;
        
        final boolean canRegen = plotworld.TYPE == 0 && plotworld.TERRAIN == 0;
        
        final PlotBlock[] plotfloor = dpw.TOP_BLOCK;
        final PlotBlock[] filling = dpw.MAIN_BLOCK;
        final PlotBlock[] bedrock = (dpw.PLOT_BEDROCK ? new PlotBlock[] { new PlotBlock((short) 7, (byte) 0) } : filling);
        final PlotBlock air = new PlotBlock((short) 0, (byte) 0);
        
        final ArrayList<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
        
        for (int x = bcx; x <= tcx; x++) {
            for (int z = bcz; z <= tcz; z++) {
                chunks.add(new ChunkLoc(x, z));
            }
        }
        
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (chunks.size() > 0 && System.currentTimeMillis() - start < 20) {
                    ChunkLoc chunk = chunks.remove(0);
                    int x = chunk.x;
                    int z = chunk.z;
                    int xxb = x << 4;
                    int zzb = z << 4;
                    int xxt = xxb + 15;
                    int zzt = zzb + 15;
                    if (canRegen) {
                        if (xxb >= p1x && xxt <= p2x && zzb >= p1z && zzt <= p2z) {
                            BukkitUtil.regenerateChunk(world, x, z);
                            continue;
                        }
                    }
                    if (x == bcx) {
                        xxb = p1x; 
                    }
                    if (x == tcx) {
                        xxt = p2x;
                    }
                    if (z == bcz) {
                        zzb = p1z;
                    }
                    if (z == tcz) {
                        zzt = p2z;
                    }
                    BukkitUtil.setBiome(plot.world, xxb, zzb, xxt, zzt, dpw.PLOT_BIOME);
                    Location bot = new Location(world, xxb, 0, zzb);
                    Location top = new Location(world, xxt + 1, 1, zzt + 1);
                    MainUtil.setCuboidAsync(world, bot, top, bedrock);
                    bot.setY(1);
                    top.setY(dpw.PLOT_HEIGHT);
                    MainUtil.setCuboidAsync(world, bot, top, filling);
                    bot.setY(dpw.PLOT_HEIGHT);
                    top.setY(dpw.PLOT_HEIGHT + 1);
                    MainUtil.setCuboidAsync(world, bot, top, plotfloor);
                    bot.setY(dpw.PLOT_HEIGHT + 1);
                    top.setY(256);
                    MainUtil.setSimpleCuboidAsync(world, bot, top, air);
                }
                if (chunks.size() != 0) {
                    TaskManager.runTaskLater(this, 1);
                }
                else {
                    pastePlotSchematic(dpw, pos1, pos2);
                    final PlotBlock wall = isDelete ? dpw.WALL_BLOCK : dpw.CLAIMED_WALL_BLOCK;
                    setWall(dpw, plot.id, new PlotBlock[] { wall });
                    SetBlockQueue.addNotify(whenDone);
                }
            }
        });
        return true;
    }
    
    public void pastePlotSchematic(HybridPlotWorld plotworld, Location l1, Location l2) {
        if (!plotworld.PLOT_SCHEMATIC) {
            return;
        }
        createSchemAbs(plotworld, l1.add(1,0,1), l2, plotworld.PLOT_HEIGHT, false);
    }
    
}
