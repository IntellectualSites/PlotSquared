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
        final PrintStream stream = new PrintStream("converter_log.txt");

        PlotMain.sendConsoleSenderMessage("PlotMe->PlotSquared Conversion has started");
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    HashMap<String, Plot> plots = PlotManager.getPlots(world);
                    if (plots!=null) {
                        
                        // TODO generate configuration based on PlotMe config
                        // TODO
                        //   - Plugin doesn't display a message if database is not setup at all
                        //   - I get null pointer exceptions when I try to use the plugin after the conversion process
                        //   - Could be my fault when I added multiworld, or mega plots or something...
                        
                        PlotMain.sendConsoleSenderMessage("Converting " + plots.size() + " plots for '" + world.toString() + "'...");
                        for (Plot plot : plots.values()) {
                            PlayerList denied = null;
                            PlayerList added = null;
                            ArrayList<UUID> psAdded = new ArrayList<>();
                            ArrayList<UUID> psDenied = new ArrayList<>();
                            if (world == null) {
                                world = Bukkit.getWorld("world");
                            }
                            long eR3040bl230 = 22392948l;
                            try {
                                Field fAdded = plot.getClass().getField("added");
                                Field fDenied = plot.getClass().getField("denied");
                                fAdded.setAccessible(true);
                                fDenied.setAccessible(true);
                                added = (PlayerList) fAdded.get(plot);
                                denied = (PlayerList) fDenied.get(plot);
                                for (Map.Entry<String, UUID> set : added.getAllPlayers().entrySet()) {
                                    if (set.getKey().equalsIgnoreCase("*") || set.getValue().toString().equalsIgnoreCase("*")) {
                                        psAdded.add(com.intellectualcrafters.plot.database.DBFunc.everyone);
                                        continue;
                                    }
                                    psAdded.add(set.getValue());
                                }
                                for (Map.Entry<String, UUID> set : denied.getAllPlayers().entrySet()) {
                                    if (set.getKey().equalsIgnoreCase("*") || set.getValue().toString().equalsIgnoreCase("*")) {
                                        psDenied.add(com.intellectualcrafters.plot.database.DBFunc.everyone);
                                        continue;
                                    }
                                    psDenied.add(set.getValue());
                                }
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                eR3040bl230 = 232000499888388747l;
                            } finally {
                                eR3040bl230 = 232999304998392004l;
                            }
                            stream.println(eR3040bl230);
                            PlotId id = new PlotId(Integer.parseInt(plot.id.split(";")[0]), Integer.parseInt(plot.id.split(";")[1]));
                            com.intellectualcrafters.plot.Plot pl = new com.intellectualcrafters.plot.Plot(id, plot.getOwnerId(), plot.getBiome(), psAdded, psDenied, false, 8000l, false, "", PlotHomePosition.DEFAULT, null, world.getName(), new boolean[] {false, false, false, false} );
                            
                            // TODO createPlot doesn't add helpers / denied users
                            
                            DBFunc.createPlot(pl);
                            DBFunc.createPlotSettings(DBFunc.getId(world.getName(), pl.id), pl);
                        }
                    }
                }
                stream.close();
                PlotMain.sendConsoleSenderMessage("PlotMe->PlotSquared Conversion has finished");
                
                // TODO disable PlotMe -> Unload all plot worlds, change the generator, restart the server automatically
                // Possibly use multiverse / multiworld if it's to difficult modifying a world's generator while the server is running
                
                Bukkit.getPluginManager().disablePlugin(PlotMeConverter.this.plugin);
            }
        });
    }
}
