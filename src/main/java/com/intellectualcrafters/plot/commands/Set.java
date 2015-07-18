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

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.listeners.APlotListener;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
public class Set extends SubCommand {
    public final static String[] values = new String[] { "biome", "alias", "home", "flag" };
    public final static String[] aliases = new String[] { "b", "w", "wf", "f", "a", "h", "fl" };

    public Set() {
        super(Command.SET, "Set a plot value", "set {arg} {value...}", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(plr, C.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.isAdded(plr.getUUID())) {
            if (!Permissions.hasPermission(plr, "plots.set.other")) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set.other");
                return false;
            }
        }
        if (args.length < 1) {
            PlotManager manager = PS.get().getPlotManager(loc.getWorld());
            ArrayList<String> newValues = new ArrayList<String>();
            newValues.addAll(Arrays.asList(values));
            newValues.addAll(Arrays.asList(manager.getPlotComponents(PS.get().getPlotWorld(loc.getWorld()), plot.id)));
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(newValues));
            return false;
        }
        for (int i = 0; i < aliases.length; i++) {
            if (aliases[i].equalsIgnoreCase(args[0])) {
                args[0] = values[i];
                break;
            }
        }
        if (args[0].equalsIgnoreCase("flag")) {
            if (args.length < 2) {
                final String message = StringMan.replaceFromMap("$2" + (StringUtils.join(FlagManager.getFlags(plr), "$1, $2")), C.replacements);
                // final String message = StringUtils.join(FlagManager.getFlags(plr), "&c, &6");
                MainUtil.sendMessage(plr, C.NEED_KEY.s().replaceAll("%values%", message));
                return false;
            }
            AbstractFlag af;
            try {
                af = FlagManager.getFlag(args[1].toLowerCase());
            } catch (final Exception e) {
                af = new AbstractFlag(args[1].toLowerCase());
            }
            if (!FlagManager.getFlags().contains(af) || FlagManager.isReserved(af.getKey())) {
                MainUtil.sendMessage(plr, C.NOT_VALID_FLAG);
                return false;
            }
            if (!Permissions.hasPermission(plr, "plots.set.flag." + args[1].toLowerCase())) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase());
                return false;
            }
            if (args.length == 2) {
                if (FlagManager.getPlotFlagAbs(plot, args[1].toLowerCase()) == null) {
                    MainUtil.sendMessage(plr, C.FLAG_NOT_IN_PLOT);
                    return false;
                }
                final boolean result = FlagManager.removePlotFlag(plot, args[1].toLowerCase());
                if (!result) {
                    MainUtil.sendMessage(plr, C.FLAG_NOT_REMOVED);
                    return false;
                }
                MainUtil.sendMessage(plr, C.FLAG_REMOVED);
                APlotListener.manager.plotEntry(plr, plot);
                return true;
            }
            try {
                final String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                final Object parsed_value = af.parseValueRaw(value);
                if (parsed_value == null) {
                    MainUtil.sendMessage(plr, af.getValueDesc());
                    return false;
                }
                final Flag flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), parsed_value);
                final boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    MainUtil.sendMessage(plr, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(plr, C.FLAG_ADDED);
                APlotListener.manager.plotEntry(plr, plot);
                return true;
            } catch (final Exception e) {
                MainUtil.sendMessage(plr, "&c" + e.getMessage());
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("home")) {
            if (!Permissions.hasPermission(plr, "plots.set.home")) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set.home");
                return false;
            }
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("none")) {
                    plot.setHome(null);
                    return true;
                }
                return MainUtil.sendMessage(plr, C.HOME_ARGUMENT);
            }
            //set to current location
            final String world = plr.getLocation().getWorld();
            final Location base = MainUtil.getPlotBottomLoc(world, plot.id);
            base.setY(0);
            final Location relative = plr.getLocation().subtract(base.getX(), base.getY(), base.getZ());
            final BlockLoc blockloc = new BlockLoc(relative.getX(), relative.getY(), relative.getZ(), relative.getYaw(), relative.getPitch());
            plot.setHome(blockloc);
            return MainUtil.sendMessage(plr, C.POSITION_SET);
        }
        if (args[0].equalsIgnoreCase("alias")) {
            if (!Permissions.hasPermission(plr, "plots.set.alias")) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set.alias");
                return false;
            }
            if (args.length < 2) {
                MainUtil.sendMessage(plr, C.MISSING_ALIAS);
                return false;
            }
            final String alias = args[1];
            if (alias.length() >= 50) {
                MainUtil.sendMessage(plr, C.ALIAS_TOO_LONG);
                return false;
            }
            for (final Plot p : PS.get().getPlots(plr.getLocation().getWorld()).values()) {
                if (p.settings.getAlias().equalsIgnoreCase(alias)) {
                    MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
                    return false;
                }
                if (UUIDHandler.nameExists(new StringWrapper(alias))) {
                    MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
                    return false;
                }
            }
            plot.setAlias(alias);
            MainUtil.sendMessage(plr, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));
            return true;
        }
        if (args[0].equalsIgnoreCase("biome")) {
            if (!Permissions.hasPermission(plr, "plots.set.biome")) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set.biome");
                return false;
            }
            if (args.length < 2) {
                MainUtil.sendMessage(plr, C.NEED_BIOME);
                return true;
            }
            if (args[1].length() < 2) {
                sendMessage(plr, C.NAME_LITTLE, "Biome", args[1].length() + "", "2");
                return true;
            }
            final int biome = BlockManager.manager.getBiomeFromString(args[1]);
            /*
             * for (Biome b : Biome.values()) {
             * if (b.toString().equalsIgnoreCase(args[1])) {
             * biome = b;
             * break;
             * }
             * }
             */
            if (biome == -1) {
                MainUtil.sendMessage(plr, getBiomeList(BlockManager.manager.getBiomeList()));
                return true;
            }
            plot.setBiome(args[1].toUpperCase());
            MainUtil.sendMessage(plr, C.BIOME_SET_TO.s() + args[1].toLowerCase());
            return true;
        }
        // Get components
        final String world = plr.getLocation().getWorld();
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotManager manager = PS.get().getPlotManager(world);
        final String[] components = manager.getPlotComponents(plotworld, plot.id);

        boolean allowUnsafe = DebugAllowUnsafe.unsafeAllowed.contains(plr.getUUID());

        for (final String component : components) {
            if (component.equalsIgnoreCase(args[0])) {
                if (!Permissions.hasPermission(plr, "plots.set." + component)) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.set." + component);
                    return false;
                }
                PlotBlock[] blocks;
                try {
                    if (args.length < 2) {
                        MainUtil.sendMessage(plr, C.NEED_BLOCK);
                        return true;
                    }
                    String[] split = args[1].split(",");
                    blocks = Configuration.BLOCKLIST.parseString(args[1]);
                    for (int i = 0; i < blocks.length; i++) {
                        PlotBlock block = blocks[i];
                        if (block == null) {
                            MainUtil.sendMessage(plr, C.NOT_VALID_BLOCK, split[i]);
                            String name;
                            if (split[i].contains("%")) {
                                name = split[i].split("%")[1];
                            }
                            else {
                                name = split[i];
                            }
                            StringComparison<PlotBlock>.ComparisonResult match = BlockManager.manager.getClosestBlock(name);
                            if (match != null) {
                                name = BlockManager.manager.getClosestMatchingName(match.best);
                                if (name != null) {
                                    MainUtil.sendMessage(plr, C.DID_YOU_MEAN, name.toLowerCase());
                                }
                            }
                            return false;
                        }
                        else if (!allowUnsafe && !BlockManager.manager.isBlockSolid(block)) {
                            MainUtil.sendMessage(plr, C.NOT_ALLOWED_BLOCK, block.toString());
                            return false;
                        }
                    }
                    if (!allowUnsafe) {
                        for (PlotBlock block : blocks) {
                            if (!BlockManager.manager.isBlockSolid(block)) {
                                MainUtil.sendMessage(plr, C.NOT_ALLOWED_BLOCK, block.toString());
                                return false;
                            }
                        }
                    }
                } catch (final Exception e2) {
                    MainUtil.sendMessage(plr, C.NOT_VALID_BLOCK, args[1]);
                    return false;
                }
                if (MainUtil.runners.containsKey(plot)) {
                    MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                    return false;
                }
                MainUtil.runners.put(plot, 1);
                manager.setComponent(plotworld, plot.id, component, blocks);
                MainUtil.sendMessage(plr, C.GENERATING_COMPONENT);
                SetBlockQueue.addNotify(new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.runners.remove(plot);
                    }
                });
                return true;
            }
        }
        {
            AbstractFlag af;
            try {
                af = new AbstractFlag(args[0].toLowerCase());
            } catch (final Exception e) {
                af = new AbstractFlag("");
            }
            if (FlagManager.getFlags().contains(af)) {
                final StringBuilder a = new StringBuilder();
                if (args.length > 1) {
                    for (int x = 1; x < args.length; x++) {
                        a.append(" ").append(args[x]);
                    }
                }
                MainCommand.onCommand(plr, world, ("plot set flag " + args[0] + a.toString()).split(" "));
                return true;
            }
        }
        ArrayList<String> newValues = new ArrayList<String>();
        newValues.addAll(Arrays.asList(values));
        newValues.addAll(Arrays.asList(manager.getPlotComponents(PS.get().getPlotWorld(loc.getWorld()), plot.id)));
        MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + getArgumentList(newValues));
        return false;
    }

    private String getString(final String s) {
        return StringMan.replaceAll(C.BLOCK_LIST_ITEM.s(), "%mat%", s);
    }

    private String getArgumentList(final List<String> newValues) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : newValues) {
            builder.append(getString(s));
        }
        return builder.toString().substring(1, builder.toString().length() - 1);
    }

    private String getBiomeList(final String[] biomes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(C.NEED_BIOME.s());
        for (final String b : biomes) {
            builder.append(getString(b));
        }
        return builder.toString().substring(1, builder.toString().length() - 1);
    }
}
