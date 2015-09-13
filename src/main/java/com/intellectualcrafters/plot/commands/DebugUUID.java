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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.AbstractDB;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.uuid.DefaultUUIDWrapper;
import com.plotsquared.bukkit.uuid.LowerOfflineUUIDWrapper;
import com.plotsquared.bukkit.uuid.OfflineUUIDWrapper;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "uuidconvert",
permission = "plots.admin",
description = "Debug UUID conversion",
usage = "/plot uuidconvert <lower|offline|online>",
requiredType = RequiredType.CONSOLE,
category = CommandCategory.DEBUG)
public class DebugUUID extends SubCommand {
    
    public DebugUUID() {
        requiredArguments = new Argument[] { Argument.String };
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final PlotPlayer player = null;
        
        final UUIDWrapper currentUUIDWrapper = UUIDHandler.getUUIDWrapper();
        final UUIDWrapper newWrapper;
        
        switch (args[0].toLowerCase()) {
            case "lower": {
                newWrapper = new LowerOfflineUUIDWrapper();
                break;
            }
            case "offline": {
                newWrapper = new OfflineUUIDWrapper();
                break;
            }
            case "online": {
                newWrapper = new DefaultUUIDWrapper();
                break;
            }
            default: {
                try {
                    final Class<?> clazz = Class.forName(args[0]);
                    newWrapper = (UUIDWrapper) clazz.newInstance();
                } catch (final Exception e) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot uuidconvert <lower|offline|online>");
                    return false;
                }
            }
        }
        
        if ((args.length != 2) || !args[1].equals("-o")) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot uuidconvert " + args[0] + " - o");
            MainUtil.sendMessage(player, "&cBe aware of the following!");
            MainUtil.sendMessage(player, "&8 - &cUse the database command or another method to backup your plots beforehand");
            MainUtil.sendMessage(player, "&8 - &cIf the process is interrupted, all plots could be deleted");
            MainUtil.sendMessage(player, "&8 - &cIf an error occurs, all plots could be deleted");
            MainUtil.sendMessage(player, "&8 - &cPlot settings WILL be lost upon conversion");
            MainUtil.sendMessage(player, "&cTO REITERATE: BACK UP YOUR DATABASE BEFORE USING THIS!!!");
            MainUtil.sendMessage(player, "&7Retype the command with the override parameter when ready :)");
            return false;
        }
        
        if (currentUUIDWrapper.getClass().getCanonicalName().equals(newWrapper.getClass().getCanonicalName())) {
            MainUtil.sendMessage(player, "&cUUID mode already in use!");
            return false;
        }
        MainUtil.sendConsoleMessage("&6Beginning UUID mode conversion");
        MainUtil.sendConsoleMessage("&7 - Disconnecting players");
        for (final PlotPlayer pp : UUIDHandler.getPlayers().values()) {
            pp.kick("PlotSquared UUID conversion has been initiated. You may reconnect when finished.");
        }
        
        MainUtil.sendConsoleMessage("&7 - Initializing map");
        
        final HashMap<UUID, UUID> uCMap = new HashMap<UUID, UUID>();
        final HashMap<UUID, UUID> uCReverse = new HashMap<UUID, UUID>();
        
        MainUtil.sendConsoleMessage("&7 - Collecting playerdata");
        
        final HashSet<String> worlds = new HashSet<>();
        worlds.add(Bukkit.getWorlds().get(0).getName());
        worlds.add("world");
        final HashSet<UUID> uuids = new HashSet<>();
        final HashSet<String> names = new HashSet<>();
        for (final String worldname : worlds) {
            final File playerdataFolder = new File(worldname + File.separator + "playerdata");
            String[] dat = playerdataFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            if (dat != null) {
                for (final String current : dat) {
                    final String s = current.replaceAll(".dat$", "");
                    try {
                        final UUID uuid = UUID.fromString(s);
                        uuids.add(uuid);
                    } catch (final Exception e) {
                        MainUtil.sendMessage(plr, C.PREFIX.s() + "Invalid playerdata: " + current);
                    }
                }
            }
            final File playersFolder = new File(worldname + File.separator + "players");
            dat = playersFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            if (dat != null) {
                for (final String current : dat) {
                    names.add(current.replaceAll(".dat$", ""));
                }
            }
        }
        
        MainUtil.sendConsoleMessage("&7 - Populating map");
        UUID uuid2;
        final UUIDWrapper wrapper = new DefaultUUIDWrapper();
        for (UUID uuid : uuids) {
            try {
                final OfflinePlotPlayer op = wrapper.getOfflinePlayer(uuid);
                uuid = currentUUIDWrapper.getUUID(op);
                uuid2 = newWrapper.getUUID(op);
                if (!uuid.equals(uuid2) && !uCMap.containsKey(uuid) && !uCReverse.containsKey(uuid2)) {
                    uCMap.put(uuid, uuid2);
                    uCReverse.put(uuid2, uuid);
                }
            } catch (final Throwable e) {
                MainUtil.sendMessage(plr, C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
            }
        }
        for (final String name : names) {
            final UUID uuid = currentUUIDWrapper.getUUID(name);
            uuid2 = newWrapper.getUUID(name);
            if (!uuid.equals(uuid2)) {
                uCMap.put(uuid, uuid2);
                uCReverse.put(uuid2, uuid);
            }
        }
        if (uCMap.size() == 0) {
            MainUtil.sendConsoleMessage("&c - Error! Attempting to repopulate");
            for (final OfflinePlotPlayer op : currentUUIDWrapper.getOfflinePlayers()) {
                if (op.getLastPlayed() != 0) {
                    //                    String name = op.getName();
                    //                    StringWrapper wrap = new StringWrapper(name);
                    final UUID uuid = currentUUIDWrapper.getUUID(op);
                    uuid2 = newWrapper.getUUID(op);
                    if (!uuid.equals(uuid2)) {
                        uCMap.put(uuid, uuid2);
                        uCReverse.put(uuid2, uuid);
                    }
                }
            }
            if (uCMap.size() == 0) {
                MainUtil.sendConsoleMessage("&cError. Failed to collect UUIDs!");
                return false;
            } else {
                MainUtil.sendConsoleMessage("&a - Successfully repopulated");
            }
        }
        
        MainUtil.sendConsoleMessage("&7 - Replacing cache");
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                for (final Entry<UUID, UUID> entry : uCMap.entrySet()) {
                    final String name = UUIDHandler.getName(entry.getKey());
                    if (name != null) {
                        UUIDHandler.add(new StringWrapper(name), entry.getValue());
                    }
                }
                
                MainUtil.sendConsoleMessage("&7 - Scanning for applicable files (uuids.txt)");
                
                final File file = new File(PS.get().IMP.getDirectory(), "uuids.txt");
                if (file.exists()) {
                    try {
                        final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                        for (String line : lines) {
                            try {
                                line = line.trim();
                                if (line.length() == 0) {
                                    continue;
                                }
                                line = line.replaceAll("[\\|][0-9]+[\\|][0-9]+[\\|]", "");
                                final String[] split = line.split("\\|");
                                final String name = split[0];
                                if ((name.length() == 0) || (name.length() > 16) || !StringMan.isAlphanumericUnd(name)) {
                                    continue;
                                }
                                final UUID old = currentUUIDWrapper.getUUID(name);
                                if (old == null) {
                                    continue;
                                }
                                final UUID now = newWrapper.getUUID(name);
                                UUIDHandler.add(new StringWrapper(name), now);
                                uCMap.put(old, now);
                                uCReverse.put(now, old);
                            } catch (final Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                
                MainUtil.sendConsoleMessage("&7 - Replacing wrapper");
                UUIDHandler.setUUIDWrapper(newWrapper);
                
                MainUtil.sendConsoleMessage("&7 - Updating plot objects");
                
                for (final Plot plot : PS.get().getPlotsRaw()) {
                    final UUID value = uCMap.get(plot.owner);
                    if (value != null) {
                        plot.owner = value;
                    }
                    plot.getTrusted().clear();
                    plot.getMembers().clear();
                    plot.getDenied().clear();
                }
                
                MainUtil.sendConsoleMessage("&7 - Deleting database");
                final AbstractDB database = DBFunc.dbManager;
                final boolean result = database.deleteTables();
                
                MainUtil.sendConsoleMessage("&7 - Creating tables");
                
                try {
                    database.createTables();
                    if (!result) {
                        MainUtil.sendConsoleMessage("&cConversion failed! Attempting recovery");
                        for (final Plot plot : PS.get().getPlots()) {
                            final UUID value = uCReverse.get(plot.owner);
                            if (value != null) {
                                plot.owner = value;
                            }
                        }
                        database.createPlotsAndData(new ArrayList<>(PS.get().getPlots()), new Runnable() {
                            @Override
                            public void run() {
                                MainUtil.sendMessage(null, "&6Recovery was successful!");
                            }
                        });
                        return;
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
                
                if (newWrapper instanceof OfflineUUIDWrapper) {
                    PS.get().config.set("UUID.force-lowercase", false);
                    PS.get().config.set("UUID.offline", true);
                } else if (newWrapper instanceof LowerOfflineUUIDWrapper) {
                    PS.get().config.set("UUID.force-lowercase", true);
                    PS.get().config.set("UUID.offline", true);
                } else if (newWrapper instanceof DefaultUUIDWrapper) {
                    PS.get().config.set("UUID.force-lowercase", false);
                    PS.get().config.set("UUID.offline", false);
                }
                try {
                    PS.get().config.save(PS.get().configFile);
                } catch (final Exception e) {
                    MainUtil.sendConsoleMessage("Could not save configuration. It will need to be manuall set!");
                }
                
                MainUtil.sendConsoleMessage("&7 - Populating tables");
                
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlots());
                        database.createPlotsAndData(plots, new Runnable() {
                            @Override
                            public void run() {
                                MainUtil.sendConsoleMessage("&aConversion complete!");
                            }
                        });
                    }
                });
                
                MainUtil.sendConsoleMessage("&aIt is now safe for players to join");
                MainUtil.sendConsoleMessage("&cConversion is still in progress, you will be notified when it is complete");
            }
        });
        return true;
    }
}
