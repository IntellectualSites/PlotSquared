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
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.TaskManager;

public class Trim extends SubCommand {

    public static boolean TASK = false;
    private static int TASK_ID = 0;
    
    public Trim() {
        super("trim", "plots.admin", "Delete unmodified portions of your plotworld", "trim", "", CommandCategory.DEBUG, false);
    }

    public PlotId getId(String id) {
        try {
            String[] split = id.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
        catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr != null) {
            PlayerFunctions.sendMessage(plr, (C.NOT_CONSOLE));
            return false;
        }
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            PlotId id = getId(arg);
            if (id != null) {
                PlayerFunctions.sendMessage(plr, "/plot trim x;z &l<world>");
                return false;
            }
            if (arg.equals("all")) {
                PlayerFunctions.sendMessage(plr, "/plot trim all &l<world>");
                return false;
            }
            PlayerFunctions.sendMessage(plr, C.TRIM_SYNTAX);
            return false;
        }
        if (args.length != 2) {
            PlayerFunctions.sendMessage(plr, C.TRIM_SYNTAX);
            return false;
        }
        String arg = args[0].toLowerCase();
        if (!arg.equals("all")) {
            PlayerFunctions.sendMessage(plr, C.TRIM_SYNTAX);
            return false;
        }
        final World world = Bukkit.getWorld(args[1]);
        if (world == null || PlotMain.getWorldSettings(world) == null) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_WORLD);
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
                String directory = world.getName() + File.separator + "region";
                File folder = new File(directory);
                File[] regionFiles = folder.listFiles();
                for (File file : regionFiles) {
                    String name = file.getName();
                    if (name.endsWith("mca")) {
                        if (file.getTotalSpace() <= 8192) {
                            try {
                                String[] split = name.split("\\.");
                                int x = Integer.parseInt(split[1]);
                                int z = Integer.parseInt(split[2]);
                                ChunkLoc loc = new ChunkLoc(x, z);
                                empty.add(loc);
                            }
                            catch (Exception e) {
                                System.out.print("INVALID MCA: " + name);
                            }
                        }
                        else {
                            Path path = Paths.get(file.getPath());
                            try {
                                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                                long creation = attr.creationTime().toMillis();
                                long modification = file.lastModified();
                                long diff = Math.abs(creation - modification);
                                if (diff < 10000) {
                                    try {
                                        String[] split = name.split("\\.");
                                        int x = Integer.parseInt(split[1]);
                                        int z = Integer.parseInt(split[2]);
                                        ChunkLoc loc = new ChunkLoc(x, z);
                                        empty.add(loc);
                                    }
                                    catch (Exception e) {
                                        System.out.print("INVALID MCA: " + name);
                                    }
                                }
                            } catch (Exception e) {
                                
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
    
    public static boolean getTrimRegions(final ArrayList<ChunkLoc> empty, final World world, final Runnable whenDone) {
        if (Trim.TASK) {
            return false;
        }
        final long startOld = System.currentTimeMillis();
        sendMessage("Collecting region data...");
        final ArrayList<Plot> plots = new ArrayList<>();
        plots.addAll(PlotMain.getPlots(world).values());
        final HashSet<ChunkLoc> chunks = new HashSet<>(ChunkManager.getChunkChunks(world));
        sendMessage(" - MCA #: " + chunks.size());
        sendMessage(" - CHUNKS: " + (chunks.size() * 1024) +" (max)");
        sendMessage(" - TIME ESTIMATE: " + (chunks.size()/1200) +" minutes");
        Trim.TASK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlotMain.getMain(), new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 50) {
                    if (plots.size() == 0) {
                        empty.addAll(chunks);
                        System.out.print("DONE!");
                        Trim.TASK = false;
                        TaskManager.runTaskAsync(whenDone);
                        Bukkit.getScheduler().cancelTask(Trim.TASK_ID);
                        return;
                    }
                    Plot plot = plots.get(0);
                    plots.remove(0);
                    Location pos1 = PlotHelper.getPlotBottomLoc(world, plot.id);
                    Location pos2 = PlotHelper.getPlotTopLoc(world, plot.id);
                    
                    Location pos3 = new Location(world, pos1.getBlockX(), 64, pos2.getBlockZ());
                    Location pos4 = new Location(world, pos2.getBlockX(), 64, pos1.getBlockZ());
                    
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
    
    public static void updateUnmodifiedPlots(final World world) {
        final HybridPlotManager manager = (HybridPlotManager) PlotMain.getPlotManager(world);
        final HybridPlotWorld plotworld = (HybridPlotWorld) PlotMain.getWorldSettings(world);
        final ArrayList<Plot> expired = new ArrayList<>();
        final Set<Plot> plots = ExpireManager.getOldPlots(world.getName()).keySet();
        sendMessage("Checking " + plots.size() +" plots! This may take a long time...");
        Trim.TASK_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PlotMain.getMain(), new Runnable() {
            @Override
            public void run() {
                if (manager != null && plots.size() > 0) {
                    Plot plot = plots.iterator().next();
                    if (plot.hasOwner()) {
                        HybridPlotManager.checkModified(plot, 0);
                    }
                    if (plot.owner == null || !HybridPlotManager.checkModified(plot, plotworld.REQUIRED_CHANGES)) {
                        expired.add(plot);
                        sendMessage("found expired: " + plot);
                    }
                }
                else {
                    Trim.expired = expired;
                    Trim.TASK = false;
                    sendMessage("Done!");
                    Bukkit.getScheduler().cancelTask(Trim.TASK_ID);
                    return;
                }
            }
        }, 1, 1);
    }
    
    public static void deleteChunks(World world, ArrayList<ChunkLoc> chunks) {
        String worldname = world.getName();
        for (ChunkLoc loc : chunks) {
            ChunkManager.deleteRegionFile(worldname, loc);
        }
    }
    
    public static void sendMessage(final String message) {
        PlotMain.sendConsoleSenderMessage("&3PlotSquared -> World trim&8: &7" + message);
    }
    
}
