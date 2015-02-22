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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.ChunkManager;

public class Trim extends SubCommand {
    public static boolean TASK = false;
    private static int TASK_ID = 0;
    
    public Trim() {
        super("trim", "plots.admin", "Delete unmodified portions of your plotworld", "trim", "", CommandCategory.DEBUG, false);
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
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr != null) {
            MainUtil.sendMessage(plr, (C.NOT_CONSOLE));
            return false;
        }
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
        final World world = Bukkit.getWorld(args[1]);
        if ((world == null) || (PlotSquared.getPlotWorld(world) == null)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_WORLD);
            return false;
        }
        if (Trim.TASK) {
            sendMessage(C.TRIM_IN_PROGRESS.s());
            return false;
        }
        sendMessage(C.TRIM_START.s());
        final ArrayList<ChunkLoc> empty = new ArrayList<>();
        getTrimRegions(empty, world, new Runnable() {
            @Override
            public void run() {
                deleteChunks(world, empty);
            }
        });
        return true;
    }
    
    public static boolean getBulkRegions(final ArrayList<ChunkLoc> empty, final World world, final Runnable whenDone) {
        if (Trim.TASK) {
            return false;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final String directory = world.getName() + File.separator + "region";
                final File folder = new File(directory);
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
                                System.out.print("INVALID MCA: " + name);
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
                                        System.out.print("INVALID MCA: " + name);
                                    }
                                }
                            } catch (final Exception e) {
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
        final long startOld = System.currentTimeMillis();
        sendMessage("Collecting region data...");
        final ArrayList<Plot> plots = new ArrayList<>();
        plots.addAll(PlotSquared.getPlots(world).values());
        final HashSet<ChunkLoc> chunks = new HashSet<>(AChunkManager.manager.getChunkChunks(world));
        sendMessage(" - MCA #: " + chunks.size());
        sendMessage(" - CHUNKS: " + (chunks.size() * 1024) + " (max)");
        sendMessage(" - TIME ESTIMATE: " + (chunks.size() / 1200) + " minutes");
        Trim.TASK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlotSquared.getMain(), new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < 50) {
                    if (plots.size() == 0) {
                        empty.addAll(chunks);
                        System.out.print("DONE!");
                        Trim.TASK = false;
                        TaskManager.runTaskAsync(whenDone);
                        Bukkit.getScheduler().cancelTask(Trim.TASK_ID);
                        return;
                    }
                    final Plot plot = plots.get(0);
                    plots.remove(0);
                    final Location pos1 = MainUtil.getPlotBottomLoc(world, plot.id);
                    final Location pos2 = MainUtil.getPlotTopLoc(world, plot.id);
                    final Location pos3 = new Location(world, pos1.getBlockX(), 64, pos2.getBlockZ());
                    final Location pos4 = new Location(world, pos2.getBlockX(), 64, pos1.getBlockZ());
                    chunks.remove(ChunkManager.getChunkChunk(pos1));
                    chunks.remove(ChunkManager.getChunkChunk(pos2));
                    chunks.remove(ChunkManager.getChunkChunk(pos3));
                    chunks.remove(ChunkManager.getChunkChunk(pos4));
                }
            }
        }, 20L, 20L);
        Trim.TASK = true;
        return true;
    }
    
    public static ArrayList<Plot> expired = null;
    
    //    public static void updateUnmodifiedPlots(final World world) {
    //        final SquarePlotManager manager = (SquarePlotManager) PlotSquared.getPlotManager(world);
    //        final SquarePlotWorld plotworld = (SquarePlotWorld) PlotSquared.getPlotWorld(world);
    //        final ArrayList<Plot> expired = new ArrayList<>();
    //        final Set<Plot> plots = ExpireManager.getOldPlots(world.getName()).keySet();
    //        sendMessage("Checking " + plots.size() +" plots! This may take a long time...");
    //        Trim.TASK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlotSquared.getMain(), new Runnable() {
    //            @Override
    //            public void run() {
    //                if (manager != null && plots.size() > 0) {
    //                    Plot plot = plots.iterator().next();
    //                    if (plot.hasOwner()) {
    //                        SquarePlotManager.checkModified(plot, 0);
    //                    }
    //                    if (plot.owner == null || !SquarePlotManager.checkModified(plot, plotworld.REQUIRED_CHANGES)) {
    //                        expired.add(plot);
    //                        sendMessage("found expired: " + plot);
    //                    }
    //                }
    //                else {
    //                    Trim.expired = expired;
    //                    Trim.TASK = false;
    //                    sendMessage("Done!");
    //                    Bukkit.getScheduler().cancelTask(Trim.TASK_ID);
    //                    return;
    //                }
    //            }
    //        }, 1, 1);
    //    }
    //
    public static void deleteChunks(final World world, final ArrayList<ChunkLoc> chunks) {
        final String worldname = world.getName();
        for (final ChunkLoc loc : chunks) {
            ChunkManager.deleteRegionFile(worldname, loc);
        }
    }
    
    public static void sendMessage(final String message) {
        PlotSquared.log("&3PlotSquared -> World trim&8: &7" + message);
    }
}
