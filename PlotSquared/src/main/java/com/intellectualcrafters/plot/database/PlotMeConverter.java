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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.UUIDHandler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Created 2014-08-17 for PlotSquared
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlotMeConverter {

    /**
     * PlotMain Object
     */
    private final PlotMain plugin;

    /**
     * Constructor
     *
     * @param plugin Plugin Used to run the converter
     */
    public PlotMeConverter(final PlotMain plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(final String message) {
        PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7" + message);
    }

    public void runAsync() throws Exception {
        // We have to make it wait a couple of seconds
        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    sendMessage("Conversion has started");
                    sendMessage("Connecting to PlotMe DB");
                    final ArrayList<Plot> createdPlots = new ArrayList<>();
                    final Plugin plotMePlugin = Bukkit.getPluginManager().getPlugin("PlotMe");
                    final FileConfiguration plotConfig = plotMePlugin.getConfig();
                    int count = 0;
                    
                    Connection connection;
                    if (plotConfig.getBoolean("usemySQL")) {
                        String user = plotConfig.getString("mySQLuname");
                        String password = plotConfig.getString("mySQLpass");
                        String con = plotConfig.getString("mySQLconn").replaceAll("jdbc:mysql://", "");
                        String host = con.split(":")[0];
                        String port = con.split(":")[1].split("/")[0];
                        String database = con.split(":")[1].split("/")[1];
                        MySQL mySQL = new MySQL(PlotMain.getMain(), host, port, database, user, password);
                        connection = mySQL.openConnection();
                    }
                    else {
                        connection = new SQLite(PlotMain.getMain(), plotMePlugin.getDataFolder() + File.separator +"plots.db").openConnection();
                    }
                    sendMessage("Collecting plot data");
                    ResultSet r;
                    Statement stmt;
                    HashMap<String, Integer> plotSize = new HashMap<>();
                    HashMap<String, HashMap<PlotId, Plot>> plots = new HashMap<>();
                    Set<String> worlds = plotConfig.getConfigurationSection("worlds").getKeys(false);
                    
                    stmt = connection.createStatement();
                    r = stmt.executeQuery("SELECT * FROM `plotmePlots`");
                    while (r.next()) {
                        PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                        String name = r.getString("owner");
                        System.out.print("NAME: "+name);
                        String world = r.getString("world");
                        if (!plotSize.containsKey(world)) {
                            int size = r.getInt("topZ") - r.getInt("bottomZ");
                            plotSize.put(world,size);
                            plots.put(world, new HashMap<PlotId, Plot>());
                        }
                        
                        UUID owner = UUIDHandler.getUUID(name);
                        if (owner == null) {
                            if (name.equals("*")) {
                                owner = DBFunc.everyone;
                            }
                            else {
                                sendMessage("&cCould not identify owner for plot: "+id);
                                continue;
                            }
                        }
                        Plot plot = new Plot(id, owner, new ArrayList<UUID>() , new ArrayList<UUID>(), world);
                        plots.get(world).put(id, plot);
                    }
                    
                    r = stmt.executeQuery("SELECT * FROM `plotmeAllowed`");
                    while (r.next()) {
                        count++;
                        PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                        String name = r.getString("player");
                        String world = r.getString("world");
                        UUID helper = UUIDHandler.getUUID(name);
                        if (helper == null) {
                            if (name.equals("*")) {
                                helper = DBFunc.everyone;
                            }
                            else {
                                sendMessage("&6Could not identify helper for plot: "+id);
                                continue;
                            }
                        }
                        if (plots.get(world).containsKey(id)) {
                            plots.get(world).get(id).helpers.add(helper);
                        }
                    }
                    
                    r = stmt.executeQuery("SELECT * FROM `plotmeDenied`");
                    while (r.next()) {
                        PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                        String name = r.getString("player");
                        String world = r.getString("world");
                        UUID denied = UUIDHandler.getUUID(name);
                        if (denied == null) {
                            if (name.equals("*")) {
                                denied = DBFunc.everyone;
                            }
                            else {
                                sendMessage("&6Could not identify denied for plot: "+id);
                                continue;
                            }
                        }
                        if (plots.get(world).containsKey(id)) {
                            plots.get(world).get(id).denied.add(denied);
                        }
                    }
                    
                    sendMessage("Collected " + count + "plots from PlotMe");
                    
                    for (String world : plots.keySet()) {
                        sendMessage("Copying config for: "+world);
                        try {
                            final Integer pathwidth = plotConfig.getInt("worlds." + world + ".PathWidth"); //
                            PlotMain.config.set("worlds." + world + ".road.width", pathwidth);
    
                            final Integer plotsize = plotConfig.getInt("worlds." + world + ".PlotSize"); //
                            PlotMain.config.set("worlds." + world + ".plot.size", plotsize);
    
                            final String wallblock = plotConfig.getString("worlds." + world + ".WallBlockId"); //
                            PlotMain.config.set("worlds." + world + ".wall.block", wallblock);
    
                            final String floor = plotConfig.getString("worlds." + world + ".PlotFloorBlockId"); //
                            PlotMain.config.set("worlds." + world + ".plot.floor", Arrays.asList(floor));
    
                            final String filling = plotConfig.getString("worlds." + world + ".PlotFillingBlockId"); //
                            PlotMain.config.set("worlds." + world + ".plot.filling", Arrays.asList(filling));
    
                            final String road = plotConfig.getString("worlds." + world + ".RoadMainBlockId");
                            PlotMain.config.set("worlds." + world + ".road.block", road);
    
                            final Integer height = plotConfig.getInt("worlds." + world + ".RoadHeight"); //
                            PlotMain.config.set("worlds." + world + ".road.height", height);
                        } catch (final Exception e) {
                            sendMessage("&c-- &lFailed to save configuration for world '" + world + "'\nThis will need to be done using the setup command, or manually");
                        }
                    }
                    
                    File PLOTME_DG_FILE = new File(plotMePlugin + File.separator + "PlotMe-DefaultGenerator" + File.separator + "config.yml");
                    if (PLOTME_DG_FILE.exists()) {
                        YamlConfiguration PLOTME_DG_YML = YamlConfiguration.loadConfiguration(PLOTME_DG_FILE);
                        try {
                            for (String world : plots.keySet()) {
                                final Integer pathwidth = PLOTME_DG_YML.getInt("worlds." + world + ".PathWidth"); //
                                PlotMain.config.set("worlds." + world + ".road.width", pathwidth);
        
                                final Integer plotsize = PLOTME_DG_YML.getInt("worlds." + world + ".PlotSize"); //
                                PlotMain.config.set("worlds." + world + ".plot.size", plotsize);
        
                                final String wallblock = PLOTME_DG_YML.getString("worlds." + world + ".WallBlock"); //
                                PlotMain.config.set("worlds." + world + ".wall.block", wallblock);
        
                                final String floor = PLOTME_DG_YML.getString("worlds." + world + ".PlotFloorBlock"); //
                                PlotMain.config.set("worlds." + world + ".plot.floor", Arrays.asList(floor));
        
                                final String filling = PLOTME_DG_YML.getString("worlds." + world + ".FillBlock"); //
                                PlotMain.config.set("worlds." + world + ".plot.filling", Arrays.asList(filling));
        
                                final String road = PLOTME_DG_YML.getString("worlds." + world + ".RoadMainBlock");
                                PlotMain.config.set("worlds." + world + ".road.block", road);
        
                                final Integer height = PLOTME_DG_YML.getInt("worlds." + world + ".RoadHeight"); //
                                PlotMain.config.set("worlds." + world + ".road.height", height);
                            }
                        }
                        catch (Exception e) {
                            
                        }
                    }
                    for (String world : plots.keySet()) {
                        int duplicate = 0;
                        for (Plot plot : plots.get(world).values()) {
                            if (!PlotMain.getPlots(world).containsKey(plot.id)) {
                                createdPlots.add(plot);
                            }
                            else {
                                duplicate++;
                            }
                        }
                        if (duplicate > 0) {
                            PlotMain.sendConsoleSenderMessage("&c[WARNING] Found " + duplicate + " duplicate plots already in DB for world: '" + world + "'. Have you run the converter already?");
                        }
                    }
                    
                    
                    sendMessage("Creating plot DB");
                    DBFunc.createPlots(createdPlots);
                    sendMessage("Creating settings/helpers DB");
                    DBFunc.createAllSettingsAndHelpers(createdPlots);
                    sendMessage("Saving configuration...");
                    try {
                        PlotMain.config.save(PlotMain.configFile);
                    } catch (final IOException e) {
                        sendMessage(" - &cFailed to save configuration.");
                    }

                    boolean MV = false;
                    boolean MW = false;

                    if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                        MV = true;
                    } else if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                        MW = true;
                    }

                    for (final String worldname : worlds) {
                        final World world = Bukkit.getWorld(worldname);
                        sendMessage("Reloading generator for world: '" + worldname + "'...");

                        PlotMain.removePlotWorld(worldname);

                        if (MV) {
                            // unload
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv unload " + worldname);
                            try {
                                Thread.sleep(1000);
                            } catch (final InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            // load
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv import " + worldname + " normal -g PlotSquared");
                        } else if (MW) {
                            // unload
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw unload " + worldname);
                            try {
                                Thread.sleep(1000);
                            } catch (final InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            // load
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + worldname + " plugin:PlotSquared");
                        } else {
                            Bukkit.getServer().unloadWorld(world, true);
                            final World myworld = WorldCreator.name(worldname).generator(new HybridGen(worldname)).createWorld();
                            myworld.save();
                        }
                    }

                    PlotMain.setAllPlotsRaw(DBFunc.getPlots());
                    sendMessage("Disabling PlotMe...");
                    Bukkit.getPluginManager().disablePlugin(plotMePlugin);
                    sendMessage("Conversion has finished");
                    PlotMain.sendConsoleSenderMessage("&cAlthough the server may be functional in it's current state, it is recommended that you restart the server and remove PlotMe to finalize the installation. Please make careful note of any warning messages that may have showed up during conversion.");
                    
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 20);
    }
}
