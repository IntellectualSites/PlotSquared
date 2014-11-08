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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Charsets;
import com.intellectualcrafters.plot.PlotHomePosition;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.generator.WorldGenerator;
import com.worldcretornica.plotme.PlayerList;
import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;

/**
 * Created 2014-08-17 for ${PROJECT_NAME}
 *
 * @author Citymonstret
 */
public class PlotMeConverter {

    private final PlotMain plugin;

    public PlotMeConverter(final PlotMain plugin) {
        this.plugin = plugin;
    }

    public void runAsync() throws Exception {
        Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
            @Override
            public void run() {
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Conversion has started");
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Caching playerdata...");
                final ArrayList<com.intellectualcrafters.plot.Plot> createdPlots = new ArrayList<com.intellectualcrafters.plot.Plot>();
                final boolean online = Bukkit.getServer().getOnlineMode();

                final Plugin plotMePlugin = Bukkit.getPluginManager().getPlugin("PlotMe");
                final FileConfiguration plotConfig = plotMePlugin.getConfig();

                final Set<String> worlds = new HashSet<String>();

                for (World world : Bukkit.getWorlds()) {
                    int duplicate = 0;
                    final HashMap<String, Plot> plots = PlotManager.getPlots(world);
                    if (plots != null) {

                        worlds.add(world.getName());

                        PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Converting configuration for world '" + world.getName() + "'...");

                        try {

                            final Integer pathwidth = plotConfig.getInt("worlds." + world.getName() + ".PathWidth"); //
                            PlotMain.config.set("worlds." + world.getName() + ".road.width", pathwidth);

                            final Integer plotsize = plotConfig.getInt("worlds." + world.getName() + ".PlotSize"); //
                            PlotMain.config.set("worlds." + world.getName() + ".plot.size", plotsize);

                            final String wallblock = plotConfig.getString("worlds." + world.getName() + ".WallBlockId"); //
                            PlotMain.config.set("worlds." + world.getName() + ".wall.block", wallblock);

                            final String floor = plotConfig.getString("worlds." + world.getName() + ".PlotFloorBlockId"); //
                            PlotMain.config.set("worlds." + world.getName() + ".plot.floor", Arrays.asList(new String[]{floor}));

                            final String filling = plotConfig.getString("worlds." + world.getName() + ".PlotFillingBlockId"); //
                            PlotMain.config.set("worlds." + world.getName() + ".plot.filling", Arrays.asList(new String[]{filling}));

                            final String road = plotConfig.getString("worlds." + world.getName() + ".RoadMainBlockId");
                            PlotMain.config.set("worlds." + world.getName() + ".road.block", road);

                            final String road_stripe = plotConfig.getString("worlds." + world.getName() + ".RoadStripeBlockId");
                            PlotMain.config.set("worlds." + world.getName() + ".road.stripes", road_stripe);

                            final Integer height = plotConfig.getInt("worlds." + world.getName() + ".RoadHeight"); //
                            PlotMain.config.set("worlds." + world.getName() + ".road.height", height);

                            final Boolean auto_link = plotConfig.getBoolean("worlds." + world.getName() + ".AutoLinkPlots"); //
                            PlotMain.config.set("worlds." + world.getName() + ".plot.auto_merge", auto_link);

                        } catch (final Exception e) {
                            PlotMain.sendConsoleSenderMessage(" - Failed to save configuration for world '" + world.getName() + "'. This will need to be done using the setup command or manually.");
                        }

                        PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Processing '" + plots.size() + "' plots for world '" + world.getName() + "'");

                        for (final Plot plot : plots.values()) {
                            final ArrayList<UUID> psAdded = new ArrayList<>();
                            final ArrayList<UUID> psTrusted = new ArrayList<>();
                            final ArrayList<UUID> psDenied = new ArrayList<>();
                            if (world == null) {
                                world = Bukkit.getWorld("world");
                            }
                            try {
                                if (online) {
                                    PlayerList denied = null;
                                    PlayerList added = null;
                                    final Field fAdded = plot.getClass().getDeclaredField("allowed");
                                    final Field fDenied = plot.getClass().getDeclaredField("denied");
                                    fAdded.setAccessible(true);
                                    fDenied.setAccessible(true);
                                    added = (PlayerList) fAdded.get(plot);
                                    denied = (PlayerList) fDenied.get(plot);
                                    for (final Map.Entry<String, UUID> set : added.getAllPlayers().entrySet()) {
                                        if ((set.getValue() != null) || set.getKey().equals("*")) {
                                            if (set.getKey().equalsIgnoreCase("*") || set.getValue().toString().equals("*")) {
                                                psAdded.add(DBFunc.everyone);
                                                continue;
                                            }
                                        }
                                        if (set.getValue() != null) {
                                            psAdded.add(set.getValue());
                                        }
                                    }
                                    for (final Map.Entry<String, UUID> set : denied.getAllPlayers().entrySet()) {
                                        if ((set.getValue() != null) || set.getKey().equals("*")) {
                                            if (set.getKey().equals("*") || set.getValue().toString().equals("*")) {
                                                psDenied.add(DBFunc.everyone);
                                                continue;
                                            }
                                        }
                                        if (set.getValue() != null) {
                                            psDenied.add(set.getValue());
                                        }
                                    }
                                } else {
                                    for (final String user : plot.getAllowed().split(",")) {
                                        if (user.equals("*")) {
                                            psAdded.add(DBFunc.everyone);
                                        } else {
                                            final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + user).getBytes(Charsets.UTF_8));
                                            psAdded.add(uuid);
                                        }
                                    }
                                    try {
                                        for (final String user : plot.getDenied().split(",")) {
                                            if (user.equals("*")) {
                                                psDenied.add(DBFunc.everyone);
                                            } else {
                                                final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + user).getBytes(Charsets.UTF_8));
                                                psDenied.add(uuid);
                                            }
                                        }
                                    } catch (final Throwable e) {

                                    }
                                }
                            } catch (final Throwable e) {
                                e.printStackTrace();
                            } finally {
                            }
                            final PlotId id = new PlotId(Integer.parseInt(plot.id.split(";")[0]), Integer.parseInt(plot.id.split(";")[1]));
                            com.intellectualcrafters.plot.Plot pl = null;
                            if (online) {
                                pl = new com.intellectualcrafters.plot.Plot(id, plot.getOwnerId(), plot.getBiome(), psAdded, psTrusted, psDenied,

                                        "", PlotHomePosition.DEFAULT, null, world.getName(), new boolean[]{false, false, false, false});
                            } else {
                                final String owner = plot.getOwner();
                                pl = new com.intellectualcrafters.plot.Plot(id, UUID.nameUUIDFromBytes(("OfflinePlayer:" + owner).getBytes(Charsets.UTF_8)), plot.getBiome(), psAdded, psTrusted, psDenied,

                                        "", PlotHomePosition.DEFAULT, null, world.getName(), new boolean[]{false, false, false, false});
                            }

                            if (pl != null) {
                                if (!PlotMain.getPlots(world).containsKey(id)) {
                                    createdPlots.add(pl);
                                } else {
                                    duplicate++;
                                }
                            }
                        }
                        if (duplicate > 0) {
                            PlotMain.sendConsoleSenderMessage("&c[WARNING] Found " + duplicate + " duplicate plots already in DB for world: '" + world.getName() + "'. Have you run the converter already?");
                        }
                    }
                }
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating plot DB");
                DBFunc.createPlots(createdPlots);
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating settings/helpers DB");

                // TODO createPlot doesn't add denied users
                DBFunc.createAllSettingsAndHelpers(createdPlots);
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Saving configuration...");
                try {
                    PlotMain.config.save(PlotMain.configFile);
                } catch (final IOException e) {
                    PlotMain.sendConsoleSenderMessage(" - &cFailed to save configuration.");
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
                    PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Reloading generator for world: '" + worldname + "'...");

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
                        final World myworld = WorldCreator.name(worldname).generator(new WorldGenerator(worldname)).createWorld();
                        myworld.save();
                    }
                }

                PlotMain.setAllPlotsRaw(DBFunc.getPlots());

                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Disabling PlotMe...");
                Bukkit.getPluginManager().disablePlugin(plotMePlugin);
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Conversion has finished");
                PlotMain.sendConsoleSenderMessage("&cAlthough the server may be functional in it's current state, it is recommended that you restart the server and remove PlotMe to finalize the installation. Please make careful note of any warning messages that may have showed up during conversion.");
            }
        }, 20);
    }
}
