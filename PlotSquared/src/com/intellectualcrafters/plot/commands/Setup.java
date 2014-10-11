package com.intellectualcrafters.plot.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.ConfigurationNode;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;

/**
 * Created by Citymonstret on 2014-09-26.
 */
public class Setup extends SubCommand implements Listener {

	public static Map<String, SetupObject> setupMap = new HashMap<>();

	private class SetupObject {
		String world;
		int current = 0;

		ConfigurationNode[] step;

		public SetupObject(String world, PlotWorld plotworld) {
			this.world = world;
			this.step = plotworld.getSettingNodes();
		}

		public int getCurrent() {
			return this.current;
		}

		public int getMax() {
			return this.step.length;
		}
	}

	public Setup() {
		super("setup", "plots.admin", "Setup a PlotWorld", "setup {world}",
				"setup", CommandCategory.ACTIONS);
	}

	@Override
	public boolean execute(Player plr, String... args) {
		boolean finished = false;

		if (!finished) {
			// TODO recode this to work with the multiple generators
			PlayerFunctions
					.sendMessage(plr, "&4CURRENTLY NOT IMPLEMENTED YET!");

			return false;
		}

		if (setupMap.containsKey(plr.getName())) {
			SetupObject object = setupMap.get(plr.getName());
			if (object.getCurrent() == object.getMax()) {
				sendMessage(plr, C.SETUP_FINISHED, object.world);

				ConfigurationNode[] steps = object.step;
				String world = object.world;
				for (ConfigurationNode step : steps) {
					PlotMain.config.set(
							"worlds." + world + "." + step.getConstant(),
							step.getValue());
				}
				try {
					PlotMain.config.save(PlotMain.configFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// World newWorld = WorldCreator.name(world).generator(new
				// WorldGenerator(world)).createWorld();
				// plr.teleport(newWorld.getSpawnLocation());

				setupMap.remove(plr.getName());

				return true;
			}
			ConfigurationNode step = object.step[object.current];
			if (args.length < 1) {
				sendMessage(plr, C.SETUP_STEP, object.current + 1 + "",
						step.getDescription(), step.getType(),
						step.getDefaultValue() + "");
				return true;
			} else {
				if (args[0].equalsIgnoreCase("cancel")) {
					setupMap.remove(plr.getName());
					PlayerFunctions.sendMessage(plr, "&cCancelled setup.");
					return true;
				}
				if (args[0].equalsIgnoreCase("back")) {
					if (object.current > 0) {
						object.current--;
						step = object.step[object.current];
						sendMessage(plr, C.SETUP_STEP, object.current + 1 + "",
								step.getDescription(), step.getType(),
								step.getDefaultValue() + "");
						return true;
					} else {
						sendMessage(plr, C.SETUP_STEP, object.current + 1 + "",
								step.getDescription(), step.getType(),
								step.getDefaultValue() + "");
						return true;
					}
				}
				boolean valid = step.isValid(args[0]);
				if (valid) {
					sendMessage(plr, C.SETUP_VALID_ARG, step.getConstant(),
							args[0]);
					step.setValue(args[0]);
					object.current++;
					if (object.getCurrent() == object.getMax()) {
						execute(plr, args);
						return true;
					}
					step = object.step[object.current];
					sendMessage(plr, C.SETUP_STEP, object.current + 1 + "",
							step.getDescription(), step.getType(),
							step.getDefaultValue() + "");
					return true;
				} else {
					sendMessage(plr, C.SETUP_INVALID_ARG, args[0],
							step.getConstant());
					sendMessage(plr, C.SETUP_STEP, object.current + 1 + "",
							step.getDescription(), step.getType(),
							step.getDefaultValue() + "");
					return true;
				}
			}
		} else {
			if (args.length < 1) {
				sendMessage(plr, C.SETUP_MISSING_WORLD);
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

			PlotWorld plotworld = PlotMain.getWorldSettings("//TODO"); // TODO

			setupMap.put(plr.getName(), new SetupObject(world, plotworld));
			sendMessage(plr, C.SETUP_INIT);
			SetupObject object = setupMap.get(plr.getName());
			ConfigurationNode step = object.step[object.current];
			sendMessage(plr, C.SETUP_STEP, object.current + 1 + "",
					step.getDescription(), step.getType(),
					step.getDefaultValue() + "");
			return true;
		}
	}

}
