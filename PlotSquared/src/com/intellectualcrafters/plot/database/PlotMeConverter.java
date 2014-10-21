package com.intellectualcrafters.plot.database;

import com.google.common.base.Charsets;
import com.intellectualcrafters.plot.PlotHomePosition;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.worldcretornica.plotme.PlayerList;
import com.worldcretornica.plotme.Plot;
import com.worldcretornica.plotme.PlotManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
			@Override
			public void run() {
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Conversion has started");
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Caching playerdata...");
				ArrayList<com.intellectualcrafters.plot.Plot> createdPlots =
						new ArrayList<com.intellectualcrafters.plot.Plot>();
				boolean online = Bukkit.getServer().getOnlineMode();
				
				FileConfiguration plotConfig = Bukkit.getPluginManager().getPlugin("PlotMe").getConfig();
				
				for (World world : Bukkit.getWorlds()) {
					int duplicate = 0;
					HashMap<String, Plot> plots = PlotManager.getPlots(world);
					if (plots != null) {
						
						
						
						
						PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Processing '" + plots.size()
								+ "' plots for world '" + world.getName() + "'");

						PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Converting configuration...");
						
						int pathwidth = plotConfig.getInt("worlds."+world.getName()+".PathWidth");
						int plotsize = plotConfig.getInt("worlds."+world.getName()+".PlotSize");
						int wallblock = Integer.parseInt(plotConfig.getString("worlds."+world.getName()+".WallBlockId"));
						int floor = Integer.parseInt(plotConfig.getString("worlds."+world.getName()+".PlotFloorBlockId"));
						int filling = Integer.parseInt(plotConfig.getString("worlds."+world.getName()+".PlotFillingBlockId"));
						int road = Integer.parseInt(plotConfig.getString("worlds."+world.getName()+".RoadMainBlockId"));
						int road_height = plotConfig.getInt("worlds."+world.getName()+".RoadHeight");
						
//						PlotMain.config.
						
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
								if (!PlotMain.getPlots(world).containsKey(pl.id)) {
									createdPlots.add(pl);
								}
								else {
									duplicate++;
								}
							}
						}
						if (duplicate>0) {
							PlotMain.sendConsoleSenderMessage("&4[WARNING] Found "+duplicate+" duplicate plots already in DB for world: '"+world.getName()+"'. Have you run the converter already?");
						}
					}
				}
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating plot DB");
				DBFunc.createPlots(createdPlots);
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Creating settings/helpers DB");
				
				// TODO createPlot doesn't add denied users
				DBFunc.createAllSettingsAndHelpers(createdPlots);
				stream.close();
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Copying configuration");
				
				// TODO
				
				// copy over plotme config
				
				// disable PlotMe
				
				// unload all plot worlds with MV or MW
				
				// import those worlds with MV or MW
				
				// have server owner stop the server and delete PlotMe at some point
				
				PlotMain.sendConsoleSenderMessage("&3PlotMe&8->&3PlotSquared&8: &7Conversion has finished");
				Bukkit.getPluginManager().disablePlugin(PlotMeConverter.this.plugin);
			}
		});
	}
}
