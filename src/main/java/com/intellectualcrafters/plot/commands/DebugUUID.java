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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.callers.CommandCaller;
import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.AbstractDB;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlayerManager;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.bukkit.UUIDHandler;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.LowerOfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

@CommandDeclaration(
        command = "uuidconvert",
        permission = "plots.admin",
        description = "Debug UUID conversion",
        usage = "/plot uuidconvert <lower|offline|online>",
        requiredType = PS.class,
        category = CommandCategory.DEBUG
)
public class DebugUUID extends SubCommand {

    public DebugUUID() {
        requiredArguments = new Argument[] {
                Argument.String
        };
    }

    @Override
    public boolean onCommand(final CommandCaller caller, final String[] args) {
        PlotPlayer player = null;

        UUIDWrapper currentUUIDWrapper = UUIDHandler.getUUIDWrapper();
        UUIDWrapper newWrapper = null;
        
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
                    Class<?> clazz = Class.forName(args[0]);
                    newWrapper = (UUIDWrapper) clazz.newInstance();
                }
                catch (Exception e) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot uuidconvert <lower|offline|online>");
                    return false;
                }
            }
        }
        
        if (args.length != 2 || !args[1].equals("-o")) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot uuidconvert " + args[0] + " - o");
            MainUtil.sendMessage(player, "&cBe aware of the following!");
            MainUtil.sendMessage(player, "&8 - &cIf the process is interrupted, all plots could be deleted");
            MainUtil.sendMessage(player, "&8 - &cIf an error occurs, all plots could be deleted");
            MainUtil.sendMessage(player, "&8 - &cPlot settings WILL be lost upon conversion");
            MainUtil.sendMessage(player, "&cBACK UP YOUR DATABASE BEFORE USING THIS!!!");
            MainUtil.sendMessage(player, "&7Retype the command with the override parameter when ready");
            return false;
        }
        
        if (currentUUIDWrapper.getClass().getCanonicalName().equals(newWrapper.getClass().getCanonicalName())) {
            MainUtil.sendMessage(player, "&cUUID mode already in use!");
            return false;
        }
        MainUtil.sendConsoleMessage("&6Beginning UUID mode conversion");
        MainUtil.sendConsoleMessage("&7 - Disconnecting players");
        for (PlotPlayer user : UUIDHandler.getPlayers().values()) {
            PlayerManager.manager.kickPlayer(user, "PlotSquared UUID conversion has been initiated. You may reconnect when finished.");
        }
        
        MainUtil.sendConsoleMessage("&7 - Initializing map");
        
        HashMap<UUID, UUID> uCMap = new HashMap<UUID, UUID>();
        HashMap<UUID, UUID> uCReverse = new HashMap<UUID, UUID>();
        
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
                        PS.log(C.PREFIX.s() + "Invalid playerdata: " + current);
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
                if (!uuid.equals(uuid2)) {
                    uCMap.put(uuid, uuid2);
                    uCReverse.put(uuid2, uuid);
                }
            } catch (final Throwable e) {
                PS.log(C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
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
            for (OfflinePlotPlayer op : currentUUIDWrapper.getOfflinePlayers()) {
                if (op.getLastPlayed() != 0) {
                    String name = op.getName();
                    StringWrapper wrap = new StringWrapper(name);
                    UUID uuid = currentUUIDWrapper.getUUID(op);
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
            }
            else {
                MainUtil.sendConsoleMessage("&a - Successfully repopulated");
            }
        }
        
        MainUtil.sendConsoleMessage("&7 - Replacing cache");
        for (Entry<UUID, UUID> entry : uCMap.entrySet()) {
            String name = UUIDHandler.getName(entry.getKey());
            UUIDHandler.add(new StringWrapper(name), entry.getValue());
        }
        
        MainUtil.sendConsoleMessage("&7 - Replacing wrapper");
        UUIDHandler.setUUIDWrapper(newWrapper);
        
        MainUtil.sendConsoleMessage("&7 - Updating plot objects");

        for (Plot plot : PS.get().getPlotsRaw()) {
            UUID value = uCMap.get(plot.owner);
            if (value != null) {
                plot.owner = value;
            }
            plot.getTrusted().clear();
            plot.getMembers().clear();
            plot.getDenied().clear();
        }
        
        MainUtil.sendConsoleMessage("&7 - Deleting database");
        final AbstractDB database = DBFunc.dbManager;
        boolean result = database.deleteTables();

        MainUtil.sendConsoleMessage("&7 - Creating tables");
        
        try {
            database.createTables(Settings.DB.USE_MYSQL ? "mysql" : "sqlite");
            if (!result) {
                MainUtil.sendConsoleMessage("&cConversion failed! Attempting recovery");
                for (Plot plot : PS.get().getPlots()) {
                    UUID value = uCReverse.get(plot.owner);
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
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        if (newWrapper instanceof OfflineUUIDWrapper) {
            PS.get().config.set("UUID.force-lowercase", false);
            PS.get().config.set("UUID.offline", true);
        }
        else if (newWrapper instanceof LowerOfflineUUIDWrapper) {
            PS.get().config.set("UUID.force-lowercase", true);
            PS.get().config.set("UUID.offline", true);
        }
        else if (newWrapper instanceof DefaultUUIDWrapper) {
            PS.get().config.set("UUID.force-lowercase", false);
            PS.get().config.set("UUID.offline", false);
        }
        try {
            PS.get().config.save(PS.get().configFile);
        }
        catch (Exception e) {
            MainUtil.sendConsoleMessage("Could not save configuration. It will need to be manuall set!");
        }
        
        MainUtil.sendConsoleMessage("&7 - Populating tables");
        
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlots());
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
        return true;
    }
}
