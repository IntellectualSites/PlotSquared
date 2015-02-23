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
package com.intellectualcrafters.plot.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * Created 2014-08-17 for PlotSquared
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlotMeConverter {
    /**
     * Constructor
     *
     * @param plugin Plugin Used to run the converter
     */
    private void sendMessage(final String message) {
        PlotSquared.log("&3PlotMe&8->&3PlotSquared&8: &7" + message);
    }

    public void runAsync() throws Exception {
        // We have to make it wait a couple of seconds
        TaskManager.runTaskLaterAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<Plot> createdPlots = new ArrayList<>();
                    final String dataFolder = new File(".").getAbsolutePath() + File.separator + "plugins" + File.separator + "PlotMe" + File.separator;
                    final File plotMeFile = new File(dataFolder + "config.yml");
                    if (!plotMeFile.exists()) {
                        return;
                    }
                    sendMessage("PlotMe conversion has started. To disable this, please set 'plotme-convert.enabled' in the 'settings.yml'");
                    sendMessage("Connecting to PlotMe DB");
                    final FileConfiguration plotConfig = YamlConfiguration.loadConfiguration(plotMeFile);
                    int count = 0;
                    Connection connection;
                    if (plotConfig.getBoolean("usemySQL")) {
                        final String user = plotConfig.getString("mySQLuname");
                        final String password = plotConfig.getString("mySQLpass");
                        final String con = plotConfig.getString("mySQLconn");
                        connection = DriverManager.getConnection(con, user, password);
                    } else {
                        connection = new SQLite(PlotSquared.THIS, dataFolder + File.separator + "plots.db").openConnection();
                    }
                    sendMessage("Collecting plot data");
                    sendMessage(" - plotmePlots");
                    ResultSet r;
                    Statement stmt;
                    final HashMap<String, Integer> plotSize = new HashMap<>();
                    final HashMap<String, HashMap<PlotId, Plot>> plots = new HashMap<>();
                    final Set<String> worlds = plotConfig.getConfigurationSection("worlds").getKeys(false);
                    stmt = connection.createStatement();
                    r = stmt.executeQuery("SELECT * FROM `plotmePlots`");
                    while (r.next()) {
                        count++;
                        final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                        final String name = r.getString("owner");
                        final String world = getWorld(r.getString("world"));
                        if (!plotSize.containsKey(world)) {
                            final int size = r.getInt("topZ") - r.getInt("bottomZ");
                            plotSize.put(world, size);
                            plots.put(world, new HashMap<PlotId, Plot>());
                        }
                        UUID owner = UUIDHandler.getUUID(name);
                        if (owner == null) {
                            if (name.equals("*")) {
                                owner = DBFunc.everyone;
                            } else {
                                sendMessage("&cCould not identify owner for plot: " + id + " -> " + name);
                                continue;
                            }
                        }
                        final Plot plot = new Plot(id, owner, new ArrayList<UUID>(), new ArrayList<UUID>(), world);
                        plots.get(world).put(id, plot);
                    }
                    sendMessage(" - plotmeAllowed");
                    r = stmt.executeQuery("SELECT * FROM `plotmeAllowed`");
                    while (r.next()) {
                        final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                        final String name = r.getString("player");
                        final String world = getWorld(r.getString("world"));
                        UUID helper = UUIDHandler.getUUID(name);
                        if (helper == null) {
                            if (name.equals("*")) {
                                helper = DBFunc.everyone;
                            } else {
                                sendMessage("&6Could not identify helper for plot: " + id);
                                continue;
                            }
                        }
                        if (plots.get(world).containsKey(id)) {
                            plots.get(world).get(id).helpers.add(helper);
                        }
                    }
                    sendMessage(" - plotmeDenied");
                    r = stmt.executeQuery("SELECT * FROM `plotmeDenied`");
                    while (r.next()) {
                        final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                        final String name = r.getString("player");
                        final String world = getWorld(r.getString("world"));
                        UUID denied = UUIDHandler.getUUID(name);
                        if (denied == null) {
                            if (name.equals("*")) {
                                denied = DBFunc.everyone;
                            } else {
                                sendMessage("&6Could not identify denied for plot: " + id);
                                continue;
                            }
                        }
                        if (plots.get(world).containsKey(id)) {
                            plots.get(world).get(id).denied.add(denied);
                        }
                    }
                    sendMessage("Collected " + count + " plots from PlotMe");
                    for (final String world : plots.keySet()) {
                        sendMessage("Copying config for: " + world);
                        try {
                            final String plotMeWorldName = world.toLowerCase();
                            final Integer pathwidth = plotConfig.getInt("worlds." + plotMeWorldName + ".PathWidth"); //
                            PlotSquared.config.set("worlds." + world + ".road.width", pathwidth);
                            final Integer plotsize = plotConfig.getInt("worlds." + plotMeWorldName + ".PlotSize"); //
                            PlotSquared.config.set("worlds." + world + ".plot.size", plotsize);
                            final String wallblock = plotConfig.getString("worlds." + plotMeWorldName + ".WallBlockId"); //
                            PlotSquared.config.set("worlds." + world + ".wall.block", wallblock);
                            final String floor = plotConfig.getString("worlds." + plotMeWorldName + ".PlotFloorBlockId"); //
                            PlotSquared.config.set("worlds." + world + ".plot.floor", Arrays.asList(floor));
                            final String filling = plotConfig.getString("worlds." + plotMeWorldName + ".PlotFillingBlockId"); //
                            PlotSquared.config.set("worlds." + world + ".plot.filling", Arrays.asList(filling));
                            final String road = plotConfig.getString("worlds." + plotMeWorldName + ".RoadMainBlockId");
                            PlotSquared.config.set("worlds." + world + ".road.block", road);
                            Integer height = plotConfig.getInt("worlds." + plotMeWorldName + ".RoadHeight"); //
                            if (height == null) {
                                height = 64;
                            }
                            PlotSquared.config.set("worlds." + world + ".road.height", height);
                        } catch (final Exception e) {
                            sendMessage("&c-- &lFailed to save configuration for world '" + world + "'\nThis will need to be done using the setup command, or manually");
                        }
                    }
                    final File PLOTME_DG_FILE = new File(dataFolder + File.separator + "PlotMe-DefaultGenerator" + File.separator + "config.yml");
                    if (PLOTME_DG_FILE.exists()) {
                        final YamlConfiguration PLOTME_DG_YML = YamlConfiguration.loadConfiguration(PLOTME_DG_FILE);
                        try {
                            for (final String world : plots.keySet()) {
                                final String plotMeWorldName = world.toLowerCase();
                                Integer pathwidth = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".PathWidth"); //
                                if (pathwidth == null) {
                                    pathwidth = 7;
                                }
                                PlotSquared.config.set("worlds." + world + ".road.width", pathwidth);
                                Integer plotsize = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".PlotSize"); //
                                if (plotsize == null) {
                                    plotsize = 32;
                                }
                                PlotSquared.config.set("worlds." + world + ".plot.size", plotsize);
                                String wallblock = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".WallBlock"); //
                                if (wallblock == null) {
                                    wallblock = "44";
                                }
                                PlotSquared.config.set("worlds." + world + ".wall.block", wallblock);
                                String floor = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".PlotFloorBlock"); //
                                if (floor == null) {
                                    floor = "2";
                                }
                                PlotSquared.config.set("worlds." + world + ".plot.floor", Arrays.asList(floor));
                                String filling = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".FillBlock"); //
                                if (filling == null) {
                                    filling = "3";
                                }
                                PlotSquared.config.set("worlds." + world + ".plot.filling", Arrays.asList(filling));
                                String road = PLOTME_DG_YML.getString("worlds." + plotMeWorldName + ".RoadMainBlock");
                                if (road == null) {
                                    road = "5";
                                }
                                PlotSquared.config.set("worlds." + world + ".road.block", road);
                                Integer height = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".RoadHeight"); //
                                if ((height == null) || (height == 0)) {
                                    height = PLOTME_DG_YML.getInt("worlds." + plotMeWorldName + ".GroundHeight"); //
                                    if ((height == null) || (height == 0)) {
                                        height = 64;
                                    }
                                }
                                PlotSquared.config.set("worlds." + world + ".road.height", height);
                                PlotSquared.config.set("worlds." + world + ".plot.height", height);
                                PlotSquared.config.set("worlds." + world + ".wall.height", height);
                            }
                        } catch (final Exception e) {
                        }
                    }
                    for (final String world : plots.keySet()) {
                        int duplicate = 0;
                        for (final Plot plot : plots.get(world).values()) {
                            if (!PlotSquared.getPlots(world).containsKey(plot.id)) {
                                createdPlots.add(plot);
                            } else {
                                duplicate++;
                            }
                        }
                        if (duplicate > 0) {
                            PlotSquared.log("&c[WARNING] Found " + duplicate + " duplicate plots already in DB for world: '" + world + "'. Have you run the converter already?");
                        }
                    }
                    sendMessage("Creating plot DB");
                    Thread.sleep(1000);
                    DBFunc.createPlots(createdPlots);
                    sendMessage("Creating settings/helpers DB");
                    DBFunc.createAllSettingsAndHelpers(createdPlots);
                    sendMessage("Saving configuration...");
                    try {
                        PlotSquared.config.save(PlotSquared.configFile);
                    } catch (final IOException e) {
                        sendMessage(" - &cFailed to save configuration.");
                    }
                    TaskManager.runTask(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                boolean MV = false;
                                boolean MW = false;
                                if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                                    MV = true;
                                } else if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                                    MW = true;
                                }
                                for (final String worldname : worlds) {
                                    final World world = Bukkit.getWorld(getWorld(worldname));
                                    final String actualWorldName = world.getName();
                                    sendMessage("Reloading generator for world: '" + actualWorldName + "'...");
                                    PlotSquared.removePlotWorld(actualWorldName);
                                    if (MV) {
                                        // unload
                                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv unload " + actualWorldName);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (final InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                        }
                                        // load
                                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv import " + actualWorldName + " normal -g PlotSquared");
                                    } else if (MW) {
                                        // unload
                                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw unload " + actualWorldName);
                                        try {
                                            Thread.sleep(1000);
                                        } catch (final InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                        }
                                        // load
                                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + actualWorldName + " plugin:PlotSquared");
                                    } else {
                                        Bukkit.getServer().unloadWorld(world, true);
                                        final World myworld = WorldCreator.name(actualWorldName).generator(new HybridGen(actualWorldName)).createWorld();
                                        myworld.save();
                                    }
                                }
                                PlotSquared.setAllPlotsRaw(DBFunc.getPlots());
                                sendMessage("Conversion has finished");
                                PlotSquared.log("&cPlease disable 'plotme-convert.enabled' in the settings.yml to indicate that you conversion is no longer required.");
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (final Exception e) {
                }
            }
        }, 20);
    }

    public String getWorld(final String world) {
        for (final World newworld : Bukkit.getWorlds()) {
            if (newworld.getName().equalsIgnoreCase(world)) {
                return newworld.getName();
            }
        }
        return world;
    }
}
