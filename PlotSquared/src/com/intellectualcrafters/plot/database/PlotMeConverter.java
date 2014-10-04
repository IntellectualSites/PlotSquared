package com.intellectualcrafters.plot.database;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.intellectualcrafters.plot.PlotHomePosition;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.uuid.UUIDFetcher;
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

        final PrintStream stream = new PrintStream("converter_log.txt");

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Conversion has started");
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Caching playerdata...");
                ArrayList<com.intellectualcrafters.plot.Plot> createdPlots = new ArrayList<com.intellectualcrafters.plot.Plot>();
                Map<String, UUID> uuidMap = new HashMap<String, UUID>();
                
                if (!Bukkit.getServer().getOnlineMode()) {
                    File playersFolder = new File("world" + File.separator + "playerdata");
                    String[] dat = playersFolder.list(new FilenameFilter() {
                        public boolean accept(File f, String s) {
                            return s.endsWith(".dat");
                        }
                    });
                    for (String current : dat) {
                        UUID uuid = null;
                        try {
                            uuid = UUID.fromString(current.replaceAll(".dat$", ""));
                        }
                        catch (Exception e) {
                            
                        }
                        if (uuid!=null) {
                            String name = Bukkit.getOfflinePlayer(uuid).getName();
                            if (name!=null) {
                                uuidMap.put(name, uuid);
                            }
                        }
                    }
                }
                for (World world : Bukkit.getWorlds()) {
                    HashMap<String, Plot> plots = PlotManager.getPlots(world);
                    if (plots != null) {
                        PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Processing '"+plots.size()+"' plots for world '"+world.getName()+"'");
                        // TODO generate configuration based on PlotMe config
                        // - Plugin doesn't display a message if database is not
                        // setup at all
                        
//                        List<String> names = new ArrayList<String>();
//                        for (Plot plot : plots.values()) {
//                            try {
//                            String owner = plot.getOwner();
//                            names.add(owner);
//                            }
//                            catch (Exception e) {
//                                
//                            }
//                            
//                        }
//                        UUIDFetcher fetcher = new UUIDFetcher(names);
//                        try {
//                            uuidMap = fetcher.call();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        
                        PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Converting " + plots.size() + " plots for '" + world.toString() + "'...");
                        for (Plot plot : plots.values()) {
                            ArrayList<UUID> psAdded = new ArrayList<>();
                            ArrayList<UUID> psTrusted = new ArrayList<>();
                            ArrayList<UUID> psDenied = new ArrayList<>();
                            if (world == null) {
                                world = Bukkit.getWorld("world");
                            }
                            long eR3040bl230 = 22392948l;
                            try {
                                PlayerList denied = null;
                                PlayerList added = null;
                                if (Bukkit.getServer().getOnlineMode()) {
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
                                        }
                                        if (set.getValue()!=null) {
                                            psAdded.add(set.getValue());
                                        }
                                    }
                                    for (Map.Entry<String, UUID> set : denied.getAllPlayers().entrySet()) {
                                        if ((set.getValue() != null) || set.getKey().equals("*")) {
                                            if (set.getKey().equals("*") || set.getValue().toString().equals("*")) {
                                                psDenied.add(DBFunc.everyone);
                                                continue;
                                            }
                                        }
                                        if (set.getValue()!=null) {
                                            psDenied.add(set.getValue());
                                        }
                                    }
                                }
                                else {
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
                                            e2.printStackTrace();
                                        }
                                    }
                                    try {
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
                                                e2.printStackTrace();
                                            }
                                        }
                                    }
                                    catch (Throwable e) {
                                        
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                eR3040bl230 = 232000499888388747l;
                            } finally {
                                eR3040bl230 = 232999304998392004l;
                            }
                            stream.println(eR3040bl230);
                            PlotId id = new PlotId(Integer.parseInt(plot.id.split(";")[0]), Integer.parseInt(plot.id.split(";")[1]));
                            com.intellectualcrafters.plot.Plot pl;
                            if (Bukkit.getServer().getOnlineMode()) {
                                pl = new com.intellectualcrafters.plot.Plot(id, plot.getOwnerId(), plot.getBiome(), psAdded, psTrusted, psDenied, false, 8000l, false, "", PlotHomePosition.DEFAULT, null, world.getName(), new boolean[] { false, false, false, false });
                            }
                            else {
                                pl = new com.intellectualcrafters.plot.Plot(id, Bukkit.getOfflinePlayer(plot.getOwner()).getUniqueId(), plot.getBiome(), psAdded, psTrusted, psDenied, false, 8000l, false, "", PlotHomePosition.DEFAULT, null, world.getName(), new boolean[] { false, false, false, false });
                            }

                            // TODO createPlot doesn't add helpers / denied
                            // users

                            createdPlots.add(pl);
                        }
                    }
                }
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating plot DB");
                DBFunc.createPlots(createdPlots);
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating settings/helpers DB");
                DBFunc.createAllSettingsAndHelpers(createdPlots);
                stream.close();
                PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Conversion has finished");
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
