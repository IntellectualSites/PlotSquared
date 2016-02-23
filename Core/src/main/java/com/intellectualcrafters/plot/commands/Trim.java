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
package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@CommandDeclaration(
command = "trim",
permission = "plots.admin",
description = "Delete unmodified portions of your plotworld",
usage = "/plot trim <world> [regenerate]",
requiredType = RequiredType.CONSOLE,
category = CommandCategory.ADMINISTRATION)
public class Trim extends SubCommand {
    
    public static ArrayList<Plot> expired = null;
    
    public static boolean getBulkRegions(final ArrayList<ChunkLoc> empty, final String world, final Runnable whenDone) {
        if (Trim.TASK) {
            return false;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final String directory = world + File.separator + "region";
                final File folder = new File(PS.get().IMP.getWorldContainer(), directory);
                final File[] regionFiles = folder.listFiles();
                for (final File file : regionFiles) {
                    final String name = file.getName();
                    if (name.endsWith("mca")) {
                        if (file.getTotalSpace() <= 8192) {
                            try {
                                final String[] split = name.split("\\.");
                                final int x = Integer.parseInt(split[1]);
                                final int z = Integer.parseInt(split[2]);
                                final ChunkLoc loc = new ChunkLoc(x, z);
                                empty.add(loc);
                            } catch (NumberFormatException e) {
                                PS.debug("INVALID MCA: " + name);
                            }
                        } else {
                            final Path path = Paths.get(file.getPath());
                            try {
                                final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                                final long creation = attr.creationTime().toMillis();
                                final long modification = file.lastModified();
                                final long diff = Math.abs(creation - modification);
                                if (diff < 10000) {
                                    try {
                                        final String[] split = name.split("\\.");
                                        final int x = Integer.parseInt(split[1]);
                                        final int z = Integer.parseInt(split[2]);
                                        final ChunkLoc loc = new ChunkLoc(x, z);
                                        empty.add(loc);
                                    } catch (final Exception e) {
                                        PS.debug("INVALID MCA: " + name);
                                    }
                                }
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
                Trim.TASK = false;
                TaskManager.runTaskAsync(whenDone);
            }
        });
        Trim.TASK = true;
        return true;
    }
    
    /**
     * Runs the result task with the parameters (viable, nonViable).<br>
     * @param world
     * @param result (viable = .mcr to trim, nonViable = .mcr keep)
     * @return
     */
    public static boolean getTrimRegions(final String world, final RunnableVal2<Set<ChunkLoc>, Set<ChunkLoc>> result) {
        if (result == null) {
            return false;
        }
        MainUtil.sendMessage(null, "Collecting region data...");
        final ArrayList<Plot> plots = new ArrayList<>();
        plots.addAll(PS.get().getPlots(world));
        result.value1 = new HashSet<>(ChunkManager.manager.getChunkChunks(world));
        result.value2 = new HashSet<>();
        MainUtil.sendMessage(null, " - MCA #: " + result.value1.size());
        MainUtil.sendMessage(null, " - CHUNKS: " + (result.value1.size() * 1024) + " (max)");
        MainUtil.sendMessage(null, " - TIME ESTIMATE: 12 Parsecs");
        TaskManager.objectTask(plots, new RunnableVal<Plot>() {
            @Override
            public void run(Plot plot) {
                final Location pos1 = plot.getBottom();
                final Location pos2 = plot.getTop();
                final int ccx1 = (pos1.getX() >> 9);
                final int ccz1 = (pos1.getZ() >> 9);
                final int ccx2 = (pos2.getX() >> 9);
                final int ccz2 = (pos2.getZ() >> 9);
                for (int x = ccx1; x <= ccx2; x++) {
                    for (int z = ccz1; z <= ccz2; z++) {
                        ChunkLoc loc = new ChunkLoc(x, z);
                        if (result.value1.remove(loc)) {
                            result.value2.add(loc);
                        }
                    }
                }
            }
        }, result);
        return true;
    }
    
    private static volatile boolean TASK = false;

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if (args.length == 0) {
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        final String world = args[0];
        if (!WorldUtil.IMP.isWorld(world) || (!PS.get().hasPlotArea(world))) {
            MainUtil.sendMessage(plr, C.NOT_VALID_WORLD);
            return false;
        }
        if (Trim.TASK) {
            C.TRIM_IN_PROGRESS.send(plr);
            return false;
        }
        Trim.TASK = true;
        final boolean regen = args.length == 2 && Boolean.parseBoolean(args[1]);
        getTrimRegions(world, new RunnableVal2<Set<ChunkLoc>, Set<ChunkLoc>>() {
            @Override
            public void run(final Set<ChunkLoc> viable, final Set<ChunkLoc> nonViable) {
                Runnable regenTask;
                if (regen) {
                    regenTask = new Runnable() {
                        @Override
                        public void run() {
                            if (nonViable.isEmpty()) {
                                Trim.TASK = false;
                                plr.sendMessage("Trim done!");
                                return;
                            }
                            Iterator<ChunkLoc> iter = nonViable.iterator();
                            ChunkLoc mcr = iter.next();
                            iter.remove();
                            int cbx = mcr.x << 5;
                            int cbz = mcr.z << 5;
                            // get all 1024 chunks
                            HashSet<ChunkLoc> chunks = new HashSet<>();
                            for (int x = cbx; x < cbx + 32; x++) {
                                for (int z = cbz; z < cbz + 32; z++) {
                                    ChunkLoc loc = new ChunkLoc(x, z);
                                    chunks.add(loc);
                                }
                            }
                            int bx = cbx << 4;
                            int bz = cbz << 4;
                            RegionWrapper region = new RegionWrapper(bx, bx + 511, bz, bz + 511);
                            for (Plot plot : PS.get().getPlots(world)) {
                                Location bot = plot.getBottomAbs();
                                Location top = plot.getExtendedTopAbs();
                                RegionWrapper plotReg = new RegionWrapper(bot.getX(), top.getX(), bot.getZ(), top.getZ());
                                if (!region.intersects(plotReg)) {
                                    continue;
                                }
                                for (int x = plotReg.minX >> 4; x <= plotReg.maxX >> 4; x++) {
                                    for (int z = plotReg.minZ >> 4; z <= plotReg.maxZ >> 4; z++) {
                                        ChunkLoc loc = new ChunkLoc(x, z);
                                        chunks.remove(loc);
                                    }
                                }
                            }
                            TaskManager.objectTask(chunks, new RunnableVal<ChunkLoc>() {
                                @Override
                                public void run(ChunkLoc value) {
                                    ChunkManager.manager.regenerateChunk(world, value);
                                }
                            }, this);
                        }
                    };
                }
                else {
                    regenTask = new Runnable() {
                        @Override
                        public void run() {
                            Trim.TASK = false;
                        }
                    };
                }
                ChunkManager.manager.deleteRegionFiles(world, viable, regenTask);

            }
        });
        return true;
    }
}
