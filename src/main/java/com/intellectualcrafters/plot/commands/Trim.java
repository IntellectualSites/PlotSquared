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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "trim",
permission = "plots.admin",
description = "Delete unmodified portions of your plotworld",
usage = "/plot trim",
requiredType = RequiredType.CONSOLE,
category = CommandCategory.DEBUG)
public class Trim extends SubCommand {
    
    public static boolean TASK = false;
    public static ArrayList<Plot> expired = null;
    private static int TASK_ID = 0;
    
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
                            } catch (final Exception e) {
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
                            } catch (IOException e) {
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
    
    public static boolean getTrimRegions(final ArrayList<ChunkLoc> empty, final String world, final Runnable whenDone) {
        if (Trim.TASK) {
            return false;
        }
        System.currentTimeMillis();
        sendMessage("Collecting region data...");
        final ArrayList<Plot> plots = new ArrayList<>();
        plots.addAll(PS.get().getPlots(world));
        final HashSet<ChunkLoc> chunks = new HashSet<>(ChunkManager.manager.getChunkChunks(world));
        sendMessage(" - MCA #: " + chunks.size());
        sendMessage(" - CHUNKS: " + (chunks.size() * 1024) + " (max)");
        sendMessage(" - TIME ESTIMATE: " + (chunks.size() / 1200) + " minutes");
        Trim.TASK_ID = TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < 50) {
                    if (plots.size() == 0) {
                        empty.addAll(chunks);
                        Trim.TASK = false;
                        TaskManager.runTaskAsync(whenDone);
                        PS.get().TASK.cancelTask(Trim.TASK_ID);
                        return;
                    }
                    final Plot plot = plots.remove(0);
                    
                    final Location pos1 = plot.getBottom();
                    final Location pos2 = plot.getTop();
                    
                    final int ccx1 = (pos1.getX() >> 9);
                    final int ccz1 = (pos1.getZ() >> 9);
                    final int ccx2 = (pos2.getX() >> 9);
                    final int ccz2 = (pos2.getZ() >> 9);
                    
                    for (int x = ccx1; x <= ccx2; x++) {
                        for (int z = ccz1; z <= ccz2; z++) {
                            chunks.remove(new ChunkLoc(x, z));
                        }
                    }
                }
            }
        }, 20);
        Trim.TASK = true;
        return true;
    }
    
    public static void deleteChunks(final String world, final ArrayList<ChunkLoc> chunks, final Runnable whenDone) {
        ChunkManager.manager.deleteRegionFiles(world, chunks, whenDone);
    }
    
    public static void sendMessage(final String message) {
        ConsolePlayer.getConsole().sendMessage("&3PlotSquared -> World trim&8: &7" + message);
    }
    
    public PlotId getId(final String id) {
        try {
            final String[] split = id.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } catch (final Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if (args.length == 1) {
            final String arg = args[0].toLowerCase();
            final PlotId id = getId(arg);
            if (id != null) {
                MainUtil.sendMessage(plr, "/plot trim x;z &l<world>");
                return false;
            }
            if (arg.equals("all")) {
                MainUtil.sendMessage(plr, "/plot trim all &l<world>");
                return false;
            }
            MainUtil.sendMessage(plr, C.TRIM_SYNTAX);
            return false;
        }
        if (args.length != 2) {
            MainUtil.sendMessage(plr, C.TRIM_SYNTAX);
            return false;
        }
        final String arg = args[0].toLowerCase();
        if (!arg.equals("all")) {
            MainUtil.sendMessage(plr, C.TRIM_SYNTAX);
            return false;
        }
        final String world = args[1];
        if (!WorldUtil.IMP.isWorld(world) || (!PS.get().hasPlotArea(world))) {
            MainUtil.sendMessage(plr, C.NOT_VALID_WORLD);
            return false;
        }
        if (Trim.TASK) {
            sendMessage(C.TRIM_IN_PROGRESS.s());
            return false;
        }
        sendMessage(C.TASK_START.s());
        final ArrayList<ChunkLoc> empty = new ArrayList<>();
        getTrimRegions(empty, world, new Runnable() {
            @Override
            public void run() {
                deleteChunks(world, empty, new Runnable() {
                    @Override
                    public void run() {
                        ConsolePlayer.getConsole().sendMessage("$1Trim task complete!");
                    }
                });
            }
        });
        return true;
    }
}
