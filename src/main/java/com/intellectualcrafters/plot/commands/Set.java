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
import java.util.HashSet;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "set",
description = "Set a plot value",
aliases = { "s" },
usage = "/plot set <biome|alias|home|flag> <value...>",
permission = "plots.set",
category = CommandCategory.ACTIONS,
requiredType = RequiredType.NONE)
public class Set extends SubCommand {
    public final static String[] values = new String[] { "biome", "alias", "home", "flag" };
    public final static String[] aliases = new String[] { "b", "w", "wf", "f", "a", "h", "fl" };
    
    private final SetCommand component;

    public Set() {
        component = new SetCommand() {
            
            @Override
            public String getCommand() {
                return "set.component";
            }

            @Override
            public boolean set(PlotPlayer plr, final Plot plot, String value) {
                final String world = plr.getLocation().getWorld();
                final PlotWorld plotworld = PS.get().getPlotWorld(world);
                final PlotManager manager = PS.get().getPlotManager(world);
                final String[] components = manager.getPlotComponents(plotworld, plot.id);
                final boolean allowUnsafe = DebugAllowUnsafe.unsafeAllowed.contains(plr.getUUID());
                
                String[] args = value.split(" ");
                String material = StringMan.join(Arrays.copyOfRange(args, 1, args.length), ",").trim();

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
                            final String[] split = material.split(",");
                            blocks = Configuration.BLOCKLIST.parseString(material);
                            for (int i = 0; i < blocks.length; i++) {
                                final PlotBlock block = blocks[i];
                                if (block == null) {
                                    MainUtil.sendMessage(plr, C.NOT_VALID_BLOCK, split[i]);
                                    String name;
                                    if (split[i].contains("%")) {
                                        name = split[i].split("%")[1];
                                    } else {
                                        name = split[i];
                                    }
                                    final StringComparison<PlotBlock>.ComparisonResult match = BlockManager.manager.getClosestBlock(name);
                                    if (match != null) {
                                        name = BlockManager.manager.getClosestMatchingName(match.best);
                                        if (name != null) {
                                            MainUtil.sendMessage(plr, C.DID_YOU_MEAN, name.toLowerCase());
                                        }
                                    }
                                    return false;
                                } else if (!allowUnsafe && !BlockManager.manager.isBlockSolid(block)) {
                                    MainUtil.sendMessage(plr, C.NOT_ALLOWED_BLOCK, block.toString());
                                    return false;
                                }
                            }
                            if (!allowUnsafe) {
                                for (final PlotBlock block : blocks) {
                                    if (!BlockManager.manager.isBlockSolid(block)) {
                                        MainUtil.sendMessage(plr, C.NOT_ALLOWED_BLOCK, block.toString());
                                        return false;
                                    }
                                }
                            }
                        } catch (final Exception e2) {
                            MainUtil.sendMessage(plr, C.NOT_VALID_BLOCK, material);
                            return false;
                        }
                        if (plot.getRunning() > 0) {
                            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                            return false;
                        }
                        plot.addRunning();
                        for (Plot current : MainUtil.getConnectedPlots(plot)) {
                            manager.setComponent(plotworld, current.id, component, blocks);
                        }
                        MainUtil.sendMessage(plr, C.GENERATING_COMPONENT);
                        SetBlockQueue.addNotify(new Runnable() {
                            @Override
                            public void run() {
                                plot.removeRunning();
                            }
                        });
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public boolean noArgs(PlotPlayer plr) {
        final ArrayList<String> newValues = new ArrayList<String>();
        newValues.addAll(Arrays.asList("biome", "alias", "home", "flag"));
        Plot plot = plr.getCurrentPlot();
        if (plot != null) {
            newValues.addAll(Arrays.asList(plot.getManager().getPlotComponents(plot.getWorld(), plot.id)));
        }
        MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringMan.join(newValues, C.BLOCK_LIST_SEPARATER.formatted()));
        return false;
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String... args) {
        if (args.length == 0) {
            return noArgs(plr);
        }
        Command<PlotPlayer> cmd = MainCommand.getInstance().getCommand("set" + args[0]);
        if (cmd != null) {
            return cmd.onCommand(plr, Arrays.copyOfRange(args, 1, args.length));
        }
        // Additional checks
        Plot plot = plr.getCurrentPlot();
        if (plot == null) {
            MainUtil.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        // components
        HashSet<String> components = new HashSet<String>(Arrays.asList(plot.getManager().getPlotComponents(plot.getWorld(), plot.id)));
        if (components.contains(args[0].toLowerCase())) {
            return component.onCommand(plr, Arrays.copyOfRange(args, 0, args.length));
        }
        // flag
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
                MainCommand.onCommand(plr, "plot", ("flag set " + args[0] + a.toString()).split(" "));
                return true;
            }
        }
        return noArgs(plr);
    }
}
