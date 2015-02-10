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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.util.PlayerFunctions;

public class Setup extends SubCommand {

    public final static Map<String, SetupObject> setupMap = new HashMap<>();
    public HashMap<String, PlotGenerator> generators = new HashMap<>();
    
    public Setup() {
        super("setup", "plots.admin.command.setup", "Plotworld setup command", "setup", "create", CommandCategory.ACTIONS, true);
    }
    
    private class SetupObject {
        int current = 0;
        int setup_index = 0;
        String world = null;
        String generator = null;
        int type = 0;
        int terrain = 0;
        ConfigurationNode[] step = null;
    }

    public void updateGenerators() {
        if (generators.size() > 0) {
            return;
        }
        String testWorld = "CheckingPlotSquaredGenerator";
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.isEnabled()) {
                ChunkGenerator generator = plugin.getDefaultWorldGenerator(testWorld, "");
                if (generator != null) {
                    PlotMain.removePlotWorld(testWorld);
                    final String name = plugin.getDescription().getName();
                    if (generator instanceof PlotGenerator) {
                        generators.put(name, (PlotGenerator) generator);
                    }
                }
            }
        }
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        // going through setup
        
        String name = plr.getName();
        if (!setupMap.containsKey(name)) {
            SetupObject object = new SetupObject();
            setupMap.put(name, object);
            updateGenerators();
            String prefix = "\n&8 - &7";
            sendMessage(plr, C.SETUP_INIT);
            PlayerFunctions.sendMessage(plr, "&6What generator do you want?" + prefix + StringUtils.join(generators.keySet(), prefix).replaceAll("PlotSquared", "&2PlotSquared"));
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("cancel")) {
                setupMap.remove(plr.getName());
                PlayerFunctions.sendMessage(plr, "&aCancelled setup");
                return false;
            }
            if (args[0].equalsIgnoreCase("back")) {
                SetupObject object = setupMap.get(plr.getName());
                if (object.setup_index > 0) {
                    object.setup_index--;
                    ConfigurationNode node = object.step[object.current];
                    sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", node.getDescription(), node.getType().getType(), node.getDefaultValue() + "");
                    return false;
                }
                else if (object.current > 0 ){
                    object.current--;
                }
            }
        }
        SetupObject object = setupMap.get(name);
        int index = object.current;
        switch(index) {
            case 0: { // choose generator
                if (args.length != 1 || !generators.containsKey(args[0])) {
                    String prefix = "\n&8 - &7";
                    PlayerFunctions.sendMessage(plr, "&cYou must choose a generator!" + prefix + StringUtils.join(generators.keySet(), prefix).replaceAll("PlotSquared", "&2PlotSquared"));
                    sendMessage(plr, C.SETUP_INIT);
                    return false;
                }
                object.generator = args[0];
                object.current++;
                PlayerFunctions.sendMessage(plr, "&6What world type do you want?"
                + "\n&8 - &2DEFAULT&8 - &7Standard plot generation"
                + "\n&8 - &7AUGMENTED&8 - &7Plot generation with terrain"
                + "\n&8 - &7PARTIAL&8 - &7Vanilla with clusters of plots");
                break;
            }
            case 1: { // choose world type
                List<String> types = Arrays.asList(new String[] {"default", "augmented", "partial"});
                if (args.length != 1 || !types.contains(args[0].toLowerCase())) {
                    PlayerFunctions.sendMessage(plr, "&cYou must choose a world type!"
                    + "\n&8 - &2DEFAULT&8 - &7Standard plot generation"
                    + "\n&8 - &7AUGMENTED&8 - &7Plot generation with terrain"
                    + "\n&8 - &7PARTIAL&8 - &7Vanilla with clusters of plots");
                    return false;
                }
                object.type = types.indexOf(args[0].toLowerCase());
                if (object.type == 0) {
                    object.current++;
                    if (object.step == null) {
                        object.step = generators.get(object.generator).getNewPlotWorld(null).getSettingNodes();
                    }
                    ConfigurationNode step = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                }
                else {
                    PlayerFunctions.sendMessage(plr, "&6What terrain would you like in plots?"
                    + "\n&8 - &2NONE&8 - &7No terrain at all"
                    + "\n&8 - &7ORE&8 - &7Just some ore veins and trees"
                    + "\n&8 - &7ALL&8 - &7Entirely vanilla generation");
                }
                object.current++;
                break;
            }
            case 2: { // Choose terrain
                List<String> terrain = Arrays.asList(new String[] {"none", "ore", "all"});
                if (args.length != 1 || !terrain.contains(args[0].toLowerCase())) {
                    PlayerFunctions.sendMessage(plr, "&cYou must choose the terrain!"
                    + "\n&8 - &2NONE&8 - &7No terrain at all"
                    + "\n&8 - &7ORE&8 - &7Just some ore veins and trees"
                    + "\n&8 - &7ALL&8 - &7Entirely vanilla generation");
                    return false;
                }
                object.terrain = terrain.indexOf(args[0].toLowerCase());
                object.current++;
                if (object.step == null) {
                    object.step = generators.get(object.generator).getNewPlotWorld(null).getSettingNodes();
                }
                ConfigurationNode step = object.step[object.setup_index];
                sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", step.getDescription(), step.getType().getType(), step.getDefaultValue() + "");
                break;
            }
            case 3: { // world setup
                if (object.setup_index == object.step.length) {
                    PlayerFunctions.sendMessage(plr, "&6What do you want your world to be called?");
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
                    PlayerFunctions.sendMessage(plr, "&cYou need to choose a world name!");
                    return false;
                }
                if (Bukkit.getWorld(args[0]) != null) {
                    PlayerFunctions.sendMessage(plr, "&cThat world name is already taken!");
                }
                object.world = args[0];
                setupMap.remove(plr.getName());
                World world = setupWorld(object);
                try {
                    plr.teleport(world.getSpawnLocation());
                }
                catch (Exception e) {
                    plr.sendMessage("&cAn error occured. See console for more information");
                    e.printStackTrace();
                }
                sendMessage(plr, C.SETUP_FINISHED, object.world);
                setupMap.remove(plr.getName());
            }
        }
        /*
         * 0.0 normal hybrid no clusters
         * 0.1 normal hybrid with clusters
         * 0.2 normal hybrid require clusters
         * 1.0 augmented whole world
         * 1.1 augmented whole world with ore
         * 1.2 augmented whole world with terrain
         * 2.1 augmented partial world
         * 2.2 augmented partial world with ore
         * 2.3 augmented partial world with terrain
         * 3.0 no generation + normal manager
         * 
         * generator.TYPE: PlotSquared, augmented, partial
         * generator.TERRAIN
         * 
         * WORLD.TYPE: hybrid, augmented, partial
         * if (augmented/partial)
         * WORLD.TERRAIN: 
         * 
         * 
         * types (0, 1, 2, 3)
         * 0: no options
         * 1: 
         * 
         *  
         *  - return null
         *  - schedule task to create world later
         *  - externalize multiverse/world hooks to separate class
         *  - create vanilla world
         *  - add augmented populator
         *  - add config option type
         *  - Work on heirarchy for setting nodes so you don't need to provide irrelevent info (world setup)
         *  - use code from setup command for world arguments (above) so that it persists
         *  - work on plot clearing for augmented plot worlds (terrain) (heads, banners, paintings, animals, inventoryhandler)
         *  - make a generic clear function for any generator
         *  - clean up plotmanager class (remove unnecessary methods)
         *  - make simple plot manager which can be used by external generators (don't make abstract)
         *  - plugins will override any of it's methods
         *  - make heirarchy of generators of increasing abstraction:
         *    = totally abstract (circle plots, moving plots, no tesselation)
         *    = tessellating generator
         *    = grid generator
         *    = square generator
         *    = square plot generator (must have plot section and road section) (plot height, road height)
         *    = hybrid generator
         *    
         *  - All will support whole world augmentation
         *  - Only grid will support partial plot worlds
         * 
         */
        return false;
    }
    
    public World setupWorld(SetupObject object) {
        // Configuration
        final ConfigurationNode[] steps = object.step;
        final String world = object.world;
        for (final ConfigurationNode step : steps) {
            PlotMain.config.set("worlds." + world + "." + step.getConstant(), step.getValue());
        }
        if (object.type != 0) {
            PlotMain.config.set("worlds." + world + "." + "generator.type", object.type);
            PlotMain.config.set("worlds." + world + "." + "generator.terrain", object.terrain);
            PlotMain.config.set("worlds." + world + "." + "generator.plugin", object.generator);
        }
        try {
            PlotMain.config.save(PlotMain.configFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (object.type == 0) {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal -g " + object.generator);
            } else {
                if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world + " plugin:" + object.generator);
                } else {
                    WorldCreator wc = new WorldCreator(object.world);
                    wc.generator(object.generator);
                    wc.environment(Environment.NORMAL);
                    World newWorld = Bukkit.createWorld(wc);
                }
            }
        }
        else {
            if ((Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) && Bukkit.getPluginManager().getPlugin("Multiverse-Core").isEnabled()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mv create " + world + " normal");
            } else {
                if ((Bukkit.getPluginManager().getPlugin("MultiWorld") != null) && Bukkit.getPluginManager().getPlugin("MultiWorld").isEnabled()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mw create " + world);
                } else {
                    Bukkit.createWorld(new WorldCreator(object.world).environment(World.Environment.NORMAL));
                }
            }
        }
        return Bukkit.getWorld(object.world);
    }
}
