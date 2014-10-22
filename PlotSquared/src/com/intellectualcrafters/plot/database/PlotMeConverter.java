package com.intellectualcrafters.plot.database;

import com.google.common.base.Charsets;
import com.intellectualcrafters.plot.Configuration;
import com.intellectualcrafters.plot.ConfigurationNode;
import com.intellectualcrafters.plot.PlotBlock;
import com.intellectualcrafters.plot.PlotHomePosition;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;
import com.intellectualcrafters.plot.generator.WorldGenerator;
import com.worldcretornica.plotme.PlayerList;
import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Conversion has started");
		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Caching playerdata...");
		ArrayList<com.intellectualcrafters.plot.Plot> createdPlots =
				new ArrayList<com.intellectualcrafters.plot.Plot>();
		boolean online = Bukkit.getServer().getOnlineMode();
		
		Plugin plotMePlugin = Bukkit.getPluginManager().getPlugin("PlotMe");
		FileConfiguration plotConfig = plotMePlugin.getConfig();
		
		Set<String> worlds = new HashSet<String>();
		
		for (World world : Bukkit.getWorlds()) {
			int duplicate = 0;
			HashMap<String, Plot> plots = PlotManager.getPlots(world);
			if (plots != null) {
			    
			    worlds.add(world.getName());
			    
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Converting configuration for world '"+world.getName()+"'...");
				
				try {
				
			    Integer pathwidth = plotConfig.getInt("worlds."+world.getName()+".PathWidth"); //
			    PlotMain.config.set("worlds."+world.getName()+".road.width", pathwidth);
			    
			    Integer plotsize = plotConfig.getInt("worlds."+world.getName()+".PlotSize"); //
			    PlotMain.config.set("worlds."+world.getName()+".plot.size", plotsize);
			    
				String wallblock = plotConfig.getString("worlds."+world.getName()+".WallBlockId"); //
				PlotMain.config.set("worlds."+world.getName()+".wall.block", wallblock);
				
				String floor = plotConfig.getString("worlds."+world.getName()+".PlotFloorBlockId"); //
				PlotMain.config.set("worlds."+world.getName()+".plot.floor", Arrays.asList(new String[] {floor}));
				
				String filling = plotConfig.getString("worlds."+world.getName()+".PlotFillingBlockId"); //
				PlotMain.config.set("worlds."+world.getName()+".plot.filling", Arrays.asList(new String[] {filling}));
				
				String road = plotConfig.getString("worlds."+world.getName()+".RoadMainBlockId"); 
				PlotMain.config.set("worlds."+world.getName()+".road.block", road);
				
				String road_stripe = plotConfig.getString("worlds."+world.getName()+".RoadStripeBlockId");
				PlotMain.config.set("worlds."+world.getName()+".road.stripes", road_stripe);
				
				Integer height = plotConfig.getInt("worlds."+world.getName()+".RoadHeight"); //
				PlotMain.config.set("worlds."+world.getName()+".road.height", height);
				
				Boolean auto_link = plotConfig.getBoolean("worlds."+world.getName()+".AutoLinkPlots"); // 
				PlotMain.config.set("worlds."+world.getName()+".plot.auto_merge", auto_link);
				
				}
				catch (Exception e) {
				    PlotMain.sendConsoleSenderMessage(" - Failed to save configuration for world '"+world.getName()+"'. This will need to be done using the setup command or manually.");
				}
				
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Processing '" + plots.size() + "' plots for world '" + world.getName() + "'");
				
				for (Plot plot : plots.values()) {
					ArrayList<UUID> psAdded = new ArrayList<>();
					ArrayList<UUID> psTrusted = new ArrayList<>();
					ArrayList<UUID> psDenied = new ArrayList<>();
					if (world == null) {
						world = Bukkit.getWorld("world");
					}
					long eR3040bl230 = 22392948l;
					try {
						if (online) {
							PlayerList denied = null;
							PlayerList added = null;
							Field fAdded = plot.getClass().getDeclaredField("allowed");
							Field fDenied = plot.getClass().getDeclaredField("denied");
							fAdded.setAccessible(true);
							fDenied.setAccessible(true);
							added = (PlayerList) fAdded.get(plot);
							denied = (PlayerList) fDenied.get(plot);
							for (Map.Entry<String, UUID> set : added.getAllPlayers().entrySet()) {
								if ((set.getValue() != null) || set.getKey().equals("*")) {
									if (set.getKey().equalsIgnoreCase("*")
											|| set.getValue().toString().equals("*")) {
										psAdded.add(DBFunc.everyone);
										continue;
									}
								}
								if (set.getValue() != null) {
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
								if (set.getValue() != null) {
									psDenied.add(set.getValue());
								}
							}
						}
						else {
							for (String user : plot.getAllowed().split(",")) {
								if (user.equals("*")) {
									psAdded.add(DBFunc.everyone);
								}
								else {
									UUID uuid =
											UUID.nameUUIDFromBytes(("OfflinePlayer:" + user).getBytes(Charsets.UTF_8));
									psAdded.add(uuid);
								}
							}
							try {
								for (String user : plot.getDenied().split(",")) {
									if (user.equals("*")) {
										psDenied.add(DBFunc.everyone);
									}
									else {
										UUID uuid =
												UUID.nameUUIDFromBytes(("OfflinePlayer:" + user).getBytes(Charsets.UTF_8));
										psDenied.add(uuid);
									}
								}
							}
							catch (Throwable e) {

							}
						}
					}
					catch (Throwable e) {
						e.printStackTrace();
						eR3040bl230 = 232000499888388747l;
					}
					finally {
						eR3040bl230 = 232999304998392004l;
					}
					stream.println(eR3040bl230);
					PlotId id =
							new PlotId(Integer.parseInt(plot.id.split(";")[0]), Integer.parseInt(plot.id.split(";")[1]));
					com.intellectualcrafters.plot.Plot pl = null;
					if (online) {
						pl = new com.intellectualcrafters.plot.Plot(
								id, 
								plot.getOwnerId(), 
								plot.getBiome(), 
								psAdded, psTrusted, 
								psDenied, 

								"", 
								PlotHomePosition.DEFAULT, 
								null, 
								world.getName(), 
								new boolean[] { false, false, false, false }); 
					}
					else {
						String owner = plot.getOwner();
						pl = new com.intellectualcrafters.plot.Plot(
								id, 
								UUID.nameUUIDFromBytes(("OfflinePlayer:" + owner).getBytes(Charsets.UTF_8)), 
								plot.getBiome(), 
								psAdded, 
								psTrusted, 
								psDenied, 

								"", 
								PlotHomePosition.DEFAULT, 
								null, 
								world.getName(), 
								new boolean[] { false, false, false, false });
					}

					if (pl != null) {
						if (!PlotMain.getPlots(world).containsKey(id)) {
							createdPlots.add(pl);
						}
						else {
							duplicate++;
						}
					}
				}
				if (duplicate>0) {
					PlotMain.sendConsoleSenderMessage("&c[WARNING] Found "+duplicate+" duplicate plots already in DB for world: '"+world.getName()+"'. Have you run the converter already?");
				}
			}
		}
		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating plot DB");
		DBFunc.createPlots(createdPlots);
		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating settings/helpers DB");
		
		// TODO createPlot doesn't add denied users
		DBFunc.createAllSettingsAndHelpers(createdPlots);
		stream.close();
		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Saving configuration...");
		try {
            PlotMain.config.save(PlotMain.configFile);
        } catch (IOException e) {
            PlotMain.sendConsoleSenderMessage(" - &cFailed to save configuration.");
        }
		
		boolean MV = false;
		boolean MW = false;
		
		if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
            MV = true;
        }
        else {
            MW = true;
        }
		
		for (String worldname : worlds) {
		    World world = Bukkit.getWorld(worldname);
		    PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Reloading generator for world: '"+worldname+"'...");
		    
		    PlotMain.removePlotWorld(worldname);
		    
		    if (MV) {
		        // unload
		        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv unload " + worldname);
		        try {
		            Thread.sleep(1000);
		        } catch(InterruptedException ex) {
		            Thread.currentThread().interrupt();
		        }
		        // load
		        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv import " + worldname + " normal -g PlotSquared");
		    }
		    else if (MW) {
		        // unload
		        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw unload " + worldname);
		        try {
		            Thread.sleep(1000);
		        } catch(InterruptedException ex) {
		            Thread.currentThread().interrupt();
		        }
		        // load
		        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + worldname+" plugin:PlotSquared");
		    }
		    else {
		        Bukkit.getServer().unloadWorld(world, true);
                World myworld = WorldCreator.name(worldname).generator(new WorldGenerator(worldname)).createWorld();
                myworld.save();
		    }
		}
		
		PlotMain.setAllPlotsRaw(DBFunc.getPlots());
		
		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Disabling PlotMe...");
		Bukkit.getPluginManager().disablePlugin(plotMePlugin);
		PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8:&7 Conversion has finished");
		PlotMain.sendConsoleSenderMessage("&cAlthough the server may be functional in it's current state, it is recommended that you restart the server and remove PlotMe to finalize the installation. Please make careful note of any warning messages that may have showed up during conversion.");
	}
}
