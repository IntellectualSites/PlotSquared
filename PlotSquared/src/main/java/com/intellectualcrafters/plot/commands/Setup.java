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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetupUtils;

public class Setup extends SubCommand {
    public Setup() {
        super("setup", "plots.admin.command.setup", "Plotworld setup command", "setup", "create", CommandCategory.ACTIONS, true);
    }
    
    public void displayGenerators(PlotPlayer plr) {
        MainUtil.sendMessage(plr, "&6What generator do you want?");
        for (Entry<String, ChunkGenerator> entry : SetupUtils.generators.entrySet()) {
//            + prefix + StringUtils.join(SetupUtils.generators.keySet(), prefix).replaceAll("PlotSquared", "&2PlotSquared")
            if (entry.getKey().equals("PlotSquared")) {
                MainUtil.sendMessage(plr, "\n&8 - &2" + entry.getKey() + "(Hybrid Generator)");
            }
            else if (entry.getValue() instanceof HybridGen) {
                MainUtil.sendMessage(plr, "\n&8 - &7" + entry.getKey() + "(Hybrid Generator)");
            }
            else if (entry.getValue() instanceof PlotGenerator) {
                MainUtil.sendMessage(plr, "\n&8 - &7" + entry.getKey() + "(Plot Generator)");
            }
            else {
                MainUtil.sendMessage(plr, "\n&8 - &7" + entry.getKey() + "(Unknown structure)");
            }
        }
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        // going through setup
        final String name = plr.getName();
        if (!SetupUtils.setupMap.containsKey(name)) {
            final SetupObject object = new SetupObject();
            SetupUtils.setupMap.put(name, object);
            SetupUtils.manager.updateGenerators();
            sendMessage(plr, C.SETUP_INIT);
            displayGenerators(plr);
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("cancel")) {
                SetupUtils.setupMap.remove(plr.getName());
                MainUtil.sendMessage(plr, "&aCancelled setup");
                return false;
            }
            if (args[0].equalsIgnoreCase("back")) {
                final SetupObject object = SetupUtils.setupMap.get(plr.getName());
                if (object.setup_index > 0) {
                    object.setup_index--;
                    final ConfigurationNode node = object.step[object.current];
                    sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", node.getDescription(), node.getType().getType(), node.getDefaultValue() + "");
                    return false;
                } else if (object.current > 0) {
                    object.current--;
                }
            }
        }
        final SetupObject object = SetupUtils.setupMap.get(name);
        final int index = object.current;
        switch (index) {
            case 0: { // choose generator
                if ((args.length != 1) || !SetupUtils.generators.containsKey(args[0])) {
                    final String prefix = "\n&8 - &7";
                    MainUtil.sendMessage(plr, "&cYou must choose a generator!" + prefix + StringUtils.join(SetupUtils.generators.keySet(), prefix).replaceAll("PlotSquared", "&2PlotSquared"));
                    sendMessage(plr, C.SETUP_INIT);
                    return false;
                }
                object.setupGenerator = args[0];
                object.current++;
                final String partial = Settings.ENABLE_CLUSTERS ? "\n&8 - &7PARTIAL&8 - &7Vanilla with clusters of plots" : "";
                MainUtil.sendMessage(plr, "&6What world type do you want?" + "\n&8 - &2DEFAULT&8 - &7Standard plot generation" + "\n&8 - &7AUGMENTED&8 - &7Plot generation with terrain" + partial);
                break;
            }
            case 1: { // choose world type
                List<String> allTypes = Arrays.asList(new String[] { "default", "augmented", "partial" });
                ArrayList<String> types = new ArrayList<>();
                if (SetupUtils.generators.get(object.setupGenerator) instanceof PlotGenerator) {
                    types.add("default");
                }
                types.add("augmented");
                if (Settings.ENABLE_CLUSTERS) {
                    types.add("partial");
                }
                if ((args.length != 1) || !types.contains(args[0].toLowerCase())) {
                    MainUtil.sendMessage(plr, "&cYou must choose a world type!" + "\n&8 - &2DEFAULT&8 - &7Standard plot generation" + "\n&8 - &7AUGMENTED&8 - &7Plot generation with terrain" + "\n&8 - &7PARTIAL&8 - &7Vanilla with clusters of plots");
                    return false;
                }
                object.type = allTypes.indexOf(args[0].toLowerCase());
                if (object.type == 0) {
                    object.current++;
                    if (object.step == null) {
                        ChunkGenerator gen = SetupUtils.generators.get(object.setupGenerator);
                        if (gen instanceof PlotGenerator) {
                            object.plotManager = object.setupGenerator;
                            object.step = ((PlotGenerator) SetupUtils.generators.get(object.setupGenerator)).getNewPlotWorld(null).getSettingNodes();
                            ((PlotGenerator) gen).processSetup(object, plr);
                        }
                        else {
                            
                            
                            MainUtil.sendMessage(plr, "&c[WARNING] The specified generator does not identify as PlotGenerator");
                            MainUtil.sendMessage(plr, "&7Searching for a configuration script...");
                            
                            boolean script = false;
                            // TODO allow external configuration scripts
                            
                            MainUtil.sendMessage(plr, "&cNo script has been found:");
                            MainUtil.sendMessage(plr, "&7 - You may need to manually configure the other plugin");
                            object.step = ((PlotGenerator) SetupUtils.generators.get("PlotSquared")).getNewPlotWorld(null).getSettingNodes();
                        }
                    }
                    final ConfigurationNode step = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                } else {
                    MainUtil.sendMessage(plr, "&6What terrain would you like in plots?" + "\n&8 - &2NONE&8 - &7No terrain at all" + "\n&8 - &7ORE&8 - &7Just some ore veins and trees" + "\n&8 - &7ALL&8 - &7Entirely vanilla generation");
                }
                object.current++;
                break;
            }
            case 2: { // Choose terrain
                final List<String> terrain = Arrays.asList(new String[] { "none", "ore", "all" });
                if ((args.length != 1) || !terrain.contains(args[0].toLowerCase())) {
                    MainUtil.sendMessage(plr, "&cYou must choose the terrain!" + "\n&8 - &2NONE&8 - &7No terrain at all" + "\n&8 - &7ORE&8 - &7Just some ore veins and trees" + "\n&8 - &7ALL&8 - &7Entirely vanilla generation");
                    return false;
                }
                object.terrain = terrain.indexOf(args[0].toLowerCase());
                object.current++;
                if (object.step == null) {
                    object.step = SetupUtils.generators.get(object.generator).getNewPlotWorld(null).getSettingNodes();
                }
                final ConfigurationNode step = object.step[object.setup_index];
                sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                break;
            }
            case 3: { // world setup
                if (object.setup_index == object.step.length) {
                    MainUtil.sendMessage(plr, "&6What do you want your world to be called?");
                    object.setup_index = 0;
                    object.current++;
                    return true;
                }
                ConfigurationNode step = object.step[object.setup_index];
                if (args.length < 1) {
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return false;
                }
                final boolean valid = step.isValid(args[0]);
                if (valid) {
                    sendMessage(plr, C.SETUP_VALID_ARG, step.getConstant(), args[0]);
                    step.setValue(args[0]);
                    object.setup_index++;
                    if (object.setup_index == object.step.length) {
                        execute(plr, args);
                        return false;
                    }
                    step = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return false;
                } else {
                    sendMessage(plr, C.SETUP_INVALID_ARG, args[0], step.getConstant());
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                    return false;
                }
            }
            case 4: {
                if (args.length != 1) {
                    MainUtil.sendMessage(plr, "&cYou need to choose a world name!");
                    return false;
                }
                if (BlockManager.manager.isWorld(args[0])) {
                    MainUtil.sendMessage(plr, "&cThat world name is already taken!");
                }
                object.world = args[0];
                SetupUtils.setupMap.remove(plr.getName());
                final String world;
                if (object.manager == null) {
                    world = SetupUtils.manager.setupWorld(object);
                }
                else {
                    
                }
                try {
                    plr.teleport(BlockManager.manager.getSpawn(world));
                } catch (final Exception e) {
                    plr.sendMessage("&cAn error occured. See console for more information");
                    e.printStackTrace();
                }
                sendMessage(plr, C.SETUP_FINISHED, object.world);
                SetupUtils.setupMap.remove(plr.getName());
            }
        }
        return false;
    }
    
}
