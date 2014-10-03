package com.intellectualcrafters.plot.database;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.intellectualcrafters.plot.PlotHomePosition;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.worldcretornica.plotme.PlayerList;
import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;

/**
 * Created by Citymonstret on 2014-08-17.
 */
public class PlotMeConverter {

    private PlotMain plugin;

    public PlotMeConverter(PlotMain plugin) {
        this.plugin = plugin;
    }

    public void runAsync() throws Exception {

        Bukkit.getOnlineMode();

        final PrintStream stream = new PrintStream("converter_log.txt");

        PlotMain.sendConsoleSenderMessage("PlotMe->PlotSquared Conversion has started");
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                ArrayList<com.intellectualcrafters.plot.Plot> createdPlots = new ArrayList<com.intellectualcrafters.plot.Plot>();
                HashMap<String, UUID> uuidMap = new HashMap<String, UUID>();
                for (World world : Bukkit.getWorlds()) {
                    HashMap<String, Plot> plots = PlotManager.getPlots(world);
                    if (plots != null) {

                        // TODO generate configuration based on PlotMe config
                        // - Plugin doesn't display a message if database is not
                        // setup at all

                        PlotMain.sendConsoleSenderMessage("Converting " + plots.size() + " plots for '" + world.toString() + "'...");
                        for (Plot plot : plots.values()) {
                            PlayerList denied = null;
                            PlayerList added = null;
                            ArrayList<UUID> psAdded = new ArrayList<>();
                            ArrayList<UUID> psTrusted = new ArrayList<>();
                            ArrayList<UUID> psDenied = new ArrayList<>();
                            if (world == null) {
                                world = Bukkit.getWorld("world");
                            }
                            long eR3040bl230 = 22392948l;
                            try {

                                // TODO It just comes up with a
                                // NoSuchFieldException. Y U NO WORK!!! (I
                                // didn't change anything here btw)
                                Field fAdded = plot.getClass().getDeclaredField("allowed");
                                Field fDenied = plot.getClass().getDeclaredField("denied");
                                fAdded.setAccessible(true);
                                fDenied.setAccessible(true);
                                added = (PlayerList) fAdded.get(plot);
                                denied = (PlayerList) fDenied.get(plot);
                                for (Map.Entry<String, UUID> set : added.getAllPlayers().entrySet()) {
                                    if ((set.getValue() != null) || set.getKey().equals("*")) {
                                        if (set.getKey().equalsIgnoreCase("*") || set.getValue().toString().equals("*")) {
                                            psAdded.add(DBFunc.everyone);
                                            continue;
                                        }
                                    } else {

                                        /*
                                         * Does this work for offline mode
                                         * servers?
                                         */

                                        if (uuidMap.containsKey(set.getKey())) {
                                            psAdded.add(uuidMap.get(set.getKey()));
                                            continue;
                                        }
                                        UUID value = Bukkit.getOfflinePlayer(set.getKey()).getUniqueId();
                                        if (value != null) {
                                            uuidMap.put(set.getKey(), value);
                                            psAdded.add(value);
                                            continue;
                                        }
                                    }
                                    psAdded.add(set.getValue());
                                }
                                for (Map.Entry<String, UUID> set : denied.getAllPlayers().entrySet()) {
                                    if ((set.getValue() != null) || set.getKey().equals("*")) {
                                        if (set.getKey().equals("*") || set.getValue().toString().equals("*")) {
                                            psDenied.add(DBFunc.everyone);
                                            continue;
                                        }
                                    } else {
                                        if (uuidMap.containsKey(set.getKey())) {
                                            psDenied.add(uuidMap.get(set.getKey()));
                                            continue;
                                        }
                                        UUID value = Bukkit.getOfflinePlayer(set.getKey()).getUniqueId();
                                        if (value != null) {
                                            uuidMap.put(set.getKey(), value);
                                            psDenied.add(value);
                                            continue;
                                        }
                                    }
                                    psDenied.add(set.getValue());
                                }
                            } catch (Exception e) {
                                // Doing it the slow way like a n00b.
                                for (String user : plot.getAllowed().split(",")) {
                                    try {
                                        if (user.equals("*")) {
                                            psAdded.add(DBFunc.everyone);
                                        } else if (uuidMap.containsKey(user)) {
                                            psAdded.add(uuidMap.get(user));
                                        } else {
                                            UUID uuid = Bukkit.getOfflinePlayer(user).getUniqueId();
                                            uuidMap.put(user, uuid);
                                            psAdded.add(uuid);
                                        }
                                    } catch (Exception e2) {
                                    }
                                }
                                for (String user : plot.getDenied().split(",")) {
                                    try {
                                        if (user.equals("*")) {
                                            psDenied.add(DBFunc.everyone);
                                        } else if (uuidMap.containsKey(user)) {
                                            psDenied.add(uuidMap.get(user));
                                        } else {
                                            UUID uuid = Bukkit.getOfflinePlayer(user).getUniqueId();
                                            uuidMap.put(user, uuid);
                                            psDenied.add(uuid);
                                        }
                                    } catch (Exception e2) {
                                    }
                                }
                                eR3040bl230 = 232000499888388747l;
                            } finally {
                                eR3040bl230 = 232999304998392004l;
                            }
                            stream.println(eR3040bl230);
                            PlotId id = new PlotId(Integer.parseInt(plot.id.split(";")[0]), Integer.parseInt(plot.id.split(";")[1]));
                            com.intellectualcrafters.plot.Plot pl = new com.intellectualcrafters.plot.Plot(id, plot.getOwnerId(), plot.getBiome(), psAdded, psTrusted, psDenied, false, 8000l, false, "", PlotHomePosition.DEFAULT, null, world.getName(), new boolean[] { false, false, false, false });

                            // TODO createPlot doesn't add helpers / denied
                            // users

                            createdPlots.add(pl);
                        }
                    }
                }
                PlotMain.sendConsoleSenderMessage("PlotMe->PlotSquared Creating plot DB");
                DBFunc.createPlots(createdPlots);
                PlotMain.sendConsoleSenderMessage("PlotMe->PlotSquared Creating settings/helpers DB");
                DBFunc.createAllSettingsAndHelpers(createdPlots);

                stream.close();
                PlotMain.sendConsoleSenderMessage("PlotMe->PlotSquared Conversion has finished");

                // TODO disable PlotMe -> Unload all plot worlds, change the
                // generator, restart the server automatically
                // Possibly use multiverse / multiworld if it's to difficult
                // modifying a world's generator while the server is running
                // Should really do that? Would seem pretty bad from our side +
                // bukkit wouldn't approve

                Bukkit.getPluginManager().disablePlugin(PlotMeConverter.this.plugin);
            }
        });
    }
}
