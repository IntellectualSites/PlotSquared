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

package com.intellectualcrafters.plot.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;

/**
 * Created 2014-09-26 for PlotSquared
 *
 * @author Citymonstret, Empire92
 */
public class Setup extends SubCommand implements Listener {

    public final static Map<String, SetupObject> setupMap = new HashMap<>();

    public Setup() {
        super("setup", "plots.admin", "Setup a PlotWorld", "setup {world} {generator}", "setup", CommandCategory.ACTIONS, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        String plrname;

        if (plr == null) {
            plrname = "";
        }
        else {
            plrname = plr.getName();
        }

        if (setupMap.containsKey(plrname)) {
            final SetupObject object = setupMap.get(plrname);
            if (object.getCurrent() == object.getMax()) {
                final ConfigurationNode[] steps = object.step;
                final String world = object.world;
                for (final ConfigurationNode step : steps) {
                    PlotMain.config.set("worlds." + world + "." + step.getConstant(), step.getValue());
                }
                try {
                    PlotMain.config.save(PlotMain.configFile);
                }
                catch (final IOException e) {
                    e.printStackTrace();
                }

                // Creating the worlds
                if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal -g " + object.plugin);
                }
                else {
                    if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world + " plugin:" + object.plugin);
                    }
                    else {
                        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                            if (plugin.isEnabled()) {
                                if (plugin.getDefaultWorldGenerator("world", "") != null) {
                                    final String name = plugin.getDescription().getName();
                                    if (object.plugin.equals(name)) {
                                        final ChunkGenerator generator = plugin.getDefaultWorldGenerator(world, "");
                                        final World myworld = WorldCreator.name(world).generator(generator).createWorld();
                                        PlayerFunctions.sendMessage(plr, "&aLoaded world.");
                                        if (plr != null) {
                                            plr.teleport(myworld.getSpawnLocation());
                                        }
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }
                sendMessage(plr, C.SETUP_FINISHED, object.world);

                setupMap.remove(plrname);

                return true;
            }
            ConfigurationNode step = object.step[object.current];
            if (args.length < 1) {
                sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
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
                        sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                        return true;
                    }
                    else {
                        sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                        return true;
                    }
                }
                final boolean valid = step.isValid(args[0]);
                if (valid) {
                    sendMessage(plr, C.SETUP_VALID_ARG, step.getConstant(), args[0]);
                    step.setValue(args[0]);
                    object.current++;
                    if (object.getCurrent() == object.getMax()) {
                        execute(plr, args);
                        return true;
                    }
                    step = object.step[object.current];
                    sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return true;
                }
                else {
                    sendMessage(plr, C.SETUP_INVALID_ARG, args[0], step.getConstant());
                    sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
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
            final String world = args[0];
            if (StringUtils.isNumeric(args[0])) {
                sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                return true;
            }

            if (PlotMain.getWorldSettings(world) != null) {
                sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                return true;
            }

            final ArrayList<String> generators = new ArrayList<>();

            ChunkGenerator generator = null;

            for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.isEnabled()) {
                    if (plugin.getDefaultWorldGenerator("world", "") != null) {
                        final String name = plugin.getDescription().getName();
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
            final SetupObject object = setupMap.get(plrname);
            final ConfigurationNode step = object.step[object.current];
            sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
            return true;
        }
    }

    private class SetupObject {
        final String              world;
        final String              plugin;
        final ConfigurationNode[] step;
        int                       current = 0;

        public SetupObject(final String world, final PlotWorld plotworld, final String plugin) {
            this.world = world;
            this.step = plotworld.getSettingNodes();
            this.plugin = plugin;
        }

        public int getCurrent() {
            return this.current;
        }

        public int getMax() {
            return this.step.length;
        }
    }

}
