package com.intellectualcrafters.plot.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.ConfigurationNode;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotGenerator;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;

/**
 * Created by Citymonstret on 2014-09-26.
 */
public class Setup extends SubCommand implements Listener {

	public static Map<String, SetupObject> setupMap = new HashMap<>();

	private class SetupObject {
		String world;
		String plugin;
		int current = 0;

		ConfigurationNode[] step;

		public SetupObject(String world, PlotWorld plotworld, String plugin) {
			this.world = world;
			this.step = plotworld.getSettingNodes();
			this.plugin = plugin;
		}

		public String getPlugin() {
			return this.plugin;
		}

		public int getCurrent() {
			return this.current;
		}

		public int getMax() {
			return this.step.length;
		}
	}

	public Setup() {
		super("setup", "plots.admin", "Setup a PlotWorld", "setup {world}", "setup", CommandCategory.ACTIONS, false);
	}

	@Override
	public boolean execute(Player plr, String... args) {
		String plrname;
		
		if (plr==null) {
			plrname = "";
		}
		else {
			plrname = plr.getName();
		}
		
		if (setupMap.containsKey(plrname)) {
			SetupObject object = setupMap.get(plrname);
			if (object.getCurrent() == object.getMax()) {
				ConfigurationNode[] steps = object.step;
				String world = object.world;
				for (ConfigurationNode step : steps) {
					PlotMain.config.set("worlds." + world + "." + step.getConstant(), step.getValue());
				}
				try {
					PlotMain.config.save(PlotMain.configFile);
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				// Creating the worlds
				if (object.getPlugin().equals("Multiverse-Core")) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world
							+ " normal -g " + object.plugin);
				}
				else
					if (object.getPlugin().equals("MultiWorld")) {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world
								+ " plugin:" + object.plugin);
					}
				sendMessage(plr, C.SETUP_FINISHED, object.world);

				setupMap.remove(plrname);

				return true;
			}
			ConfigurationNode step = object.step[object.current];
			if (args.length < 1) {
				sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue()
						+ "");
				return true;
			}
			else {
				if (args[0].equalsIgnoreCase("cancel")) {
					setupMap.remove(plrname);
					PlayerFunctions.sendMessage(plr, "&cCancelled setup.");
					return true;
				}
				if (args[0].equalsIgnoreCase("back")) {
					if (object.current > 0) {
						object.current--;
						step = object.step[object.current];
						sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue()
								+ "");
						return true;
					}
					else {
						sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue()
								+ "");
						return true;
					}
				}
				boolean valid = step.isValid(args[0]);
				if (valid) {
					sendMessage(plr, C.SETUP_VALID_ARG, step.getConstant(), args[0]);
					step.setValue(args[0]);
					object.current++;
					if (object.getCurrent() == object.getMax()) {
						execute(plr, args);
						return true;
					}
					step = object.step[object.current];
					sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue()
							+ "");
					return true;
				}
				else {
					sendMessage(plr, C.SETUP_INVALID_ARG, args[0], step.getConstant());
					sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue()
							+ "");
					return true;
				}
			}
		}
		else {
			if (args.length < 1) {
				sendMessage(plr, C.SETUP_MISSING_WORLD);
				return true;
			}
			if (args.length < 2) {
				sendMessage(plr, C.SETUP_MISSING_GENERATOR);
				return true;
			}
			String world = args[0];
			if (StringUtils.isNumeric(args[0])) {
				sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
				return true;
			}

			if (PlotMain.getWorldSettings(world) != null) {
				sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
				return true;
			}

			ArrayList<String> generators = new ArrayList<String>();

			ChunkGenerator generator = null;

			for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				if (plugin.isEnabled()) {
					if (plugin.getDefaultWorldGenerator("world", "") != null) {
						String name = plugin.getDescription().getName();
						generators.add(name);
						if (args[1].equals(name)) {
							generator = plugin.getDefaultWorldGenerator(world, "");
							break;
						}
					}

				}
			}
			if (generator == null) {
				sendMessage(plr, C.SETUP_INVALID_GENERATOR, StringUtils.join(generators, C.BLOCK_LIST_SEPARATER.s()));
				return true;
			}
			PlotWorld plotworld;
			if (generator instanceof PlotGenerator) {
				plotworld = ((PlotGenerator) generator).getNewPlotWorld(world);
			}
			else {
				plotworld = new DefaultPlotWorld(world);
			}

			setupMap.put(plrname, new SetupObject(world, plotworld, args[1]));
			sendMessage(plr, C.SETUP_INIT);
			SetupObject object = setupMap.get(plrname);
			ConfigurationNode step = object.step[object.current];
			sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue()
					+ "");
			return true;
		}
	}

}
