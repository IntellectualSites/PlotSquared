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
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.UUIDHandler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

@SuppressWarnings({"unused", "deprecated", "javadoc"}) public class Trim extends SubCommand {

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
        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_WORLD);
            return false;
        }
        HybridPlotManager manager = (HybridPlotManager) PlotMain.getPlotManager(world);
        HybridPlotWorld plotworld = (HybridPlotWorld) PlotMain.getWorldSettings(world);
        String worldname = world.getName();
        String arg = args[0].toLowerCase();
        PlotId id = getId(arg);
        if (id != null) {
            if (manager == null) {
                PlotMain.sendConsoleSenderMessage(C.NOT_VALID_HYBRID_PLOT_WORLD);
                return false;
            }
            Plot plot = PlotHelper.getPlot(world, id);
            boolean modified = false;
            if (plot.hasOwner()) {
                modified = HybridPlotManager.checkModified(plot, 0);
            }
            PlayerFunctions.sendMessage(plr, "Modified: "+modified);
            // trim ID
        }
        if (arg.equals("all")) {
            sendMessage("Initializing...");
            String directory = new File(".").getAbsolutePath() + File.separator + world.getName() + File.separator + "region";
            File folder = new File(directory);
            File[] regionFiles = folder.listFiles();
            ArrayList<ChunkLoc> chunks = new ArrayList<>();
            sendMessage("Step 1: Bulk chunk trim");
            
            int count = 0;
            for (File file : regionFiles) {
                String name = file.getName();
                if (name.endsWith("mca")) {
                    if (file.getTotalSpace() <= 8192) {
                        file.delete();
                    }
                    else {
                        boolean delete = false;
                        Path path = Paths.get(file.getPath());
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            long creation = attr.creationTime().toMillis();
                            long modification = file.lastModified();
                            long diff = Math.abs(creation - modification);
                            if (diff < 10000) {
                                count++;
                                file.delete();
                                delete = true;
                            }
                        } catch (Exception e) {
                            
                        }
                        if (!delete) {
                            String[] split = name.split("\\.");
                            try {
                                int x = Integer.parseInt(split[1]);
                                int z = Integer.parseInt(split[2]);
                                ChunkLoc loc = new ChunkLoc(x, z);
                                chunks.add(loc);
                            }
                            catch (Exception e) {  }
                        }
                    }
                }
            }
            
            count = 0;
            PlotMain.sendConsoleSenderMessage("&6 - bulk trim removed " + (count * 256) + " chunks");
            sendMessage("Step 2: Plot trim");
            {
                Set<Plot> plots = getOldPlots(world.getName());
                PlotMain.sendConsoleSenderMessage("&6 - found "+plots.size()+" expired plots");
                for (Plot plot : plots) {
                    boolean modified = false;
                    if (plot.hasOwner()) {
                        modified = HybridPlotManager.checkModified(plot, 0);
                    }
                    if (plot.owner == null || HybridPlotManager.checkModified(plot, manager.REQUIRED_CHANGES));
                }
            }
            
            
            PlotMain.sendConsoleSenderMessage("&6 - plot trim removed " + count + " plots");
            sendMessage("Step 3: Chunk trim");
            
        }
        PlayerFunctions.sendMessage(plr, C.TRIM_SYNTAX);
        return false;
    }
    
    public Set<Plot> getOldPlots(String world) {
        final Collection<Plot> plots = PlotMain.getPlots(world).values();
        final Set<Plot> toRemove = new HashSet<>();
        Set<UUID> remove = new HashSet<>();
        Set<UUID> keep = new HashSet<>();
        for (Plot plot : plots) {
            UUID uuid = plot.owner;
            if (uuid == null || remove.contains(uuid)) {
                toRemove.add(plot);
                continue;
            }
            if (keep.contains(uuid)) {
                continue;
            }
            OfflinePlayer op = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
            if (!op.hasPlayedBefore()) {
                toRemove.add(plot);
                PlotMain.removePlot(plot.world, plot.id, true);
                continue;
            }
            long last = op.getLastPlayed();
            long compared = System.currentTimeMillis() - last;
            if (TimeUnit.MILLISECONDS.toDays(compared) >= Settings.AUTO_CLEAR_DAYS) {
                toRemove.add(plot);
                remove.add(uuid);
            }
            keep.add(uuid);
        }
        return toRemove;
    }
    
    private void sendMessage(final String message) {
        PlotMain.sendConsoleSenderMessage("&3PlotSquared -> World trim&8: " + message);
    }
    
    private void runTask(final Runnable r) {
        PlotMain.getMain().getServer().getScheduler().runTaskAsynchronously(PlotMain.getMain(), r);
    }

}
