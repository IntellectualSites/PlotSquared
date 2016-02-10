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
import java.util.HashMap;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "setflag",
aliases = { "f", "flag", "setf", "setflag" },
usage = "/plot flag <set|remove|add|list|info> <flag> <value>",
description = "Set plot flags",
category = CommandCategory.ACTIONS,
requiredType = RequiredType.NONE,
permission = "plots.flag")
public class FlagCmd extends SubCommand {
    
    @Override
    public String getUsage() {
        return super.getUsage().replaceAll("<flag>", StringMan.join(FlagManager.getFlags(), "|"));
    }
    
    @Override
    public boolean onCommand(final PlotPlayer player, final String... args) {
        
        /*
         *  plot flag set fly true
         *  plot flag remove fly
         *  plot flag remove use 1,3
         *  plot flag add use 2,4
         *  plot flag list
         */
        if (args.length == 0) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
            return false;
        }
        final Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.set.flag.other")) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag.other");
            return false;
        }
        if ((args.length > 1) && FlagManager.isReserved(args[1])) {
            MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "info": {
                if (!Permissions.hasPermission(player, "plots.set.flag")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.info");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                final AbstractFlag af = FlagManager.getFlag(args[1]);
                if (af == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                // flag key
                MainUtil.sendMessage(player, C.FLAG_KEY, af.getKey());
                // flag type
                MainUtil.sendMessage(player, C.FLAG_TYPE, af.value.getClass().getSimpleName());
                // Flag type description
                MainUtil.sendMessage(player, C.FLAG_DESC, af.getValueDesc());
                return true;
            }
            case "set": {
                if (!Permissions.hasPermission(player, "plots.set.flag")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag");
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag set <flag> <value>");
                    return false;
                }
                final AbstractFlag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                final String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                if (!Permissions.hasPermission(player, "plots.set.flag." + args[1].toLowerCase() + "." + value.toLowerCase())) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase() + "." + value.toLowerCase());
                    return false;
                }
                final Object parsed = af.parseValueRaw(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + af.getValueDesc());
                    return false;
                }
                final Flag flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), parsed);
                final boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, C.FLAG_ADDED);
                return true;
            }
            case "remove": {
                if (!Permissions.hasPermission(player, "plots.flag.remove")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.remove");
                    return false;
                }
                if ((args.length != 2) && (args.length != 3)) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag remove <flag> [values]");
                    return false;
                }
                final AbstractFlag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                final Flag flag = FlagManager.getPlotFlagAbs(plot, args[1].toLowerCase());
                if (!Permissions.hasPermission(player, "plots.set.flag." + args[1].toLowerCase())) {
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase());
                        return false;
                    }
                    for (final String entry : args[2].split(",")) {
                        if (!Permissions.hasPermission(player, "plots.set.flag." + args[1].toLowerCase() + "." + entry)) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase() + "." + entry);
                            return false;
                        }
                    }
                }
                if (flag == null) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_IN_PLOT);
                    return false;
                }
                if ((args.length == 3) && flag.getAbstractFlag().isList()) {
                    final String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                    ((FlagValue.ListValue) flag.getAbstractFlag().value).remove(flag.getValue(), value);
                    DBFunc.setFlags(plot, plot.getFlags().values());
                } else {
                    final boolean result = FlagManager.removePlotFlag(plot, flag.getKey());
                    if (!result) {
                        MainUtil.sendMessage(player, C.FLAG_NOT_REMOVED);
                        return false;
                    }
                }
                MainUtil.sendMessage(player, C.FLAG_REMOVED);
                return true;
            }
            case "add": {
                if (!Permissions.hasPermission(player, "plots.flag.add")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.add");
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
                    return false;
                }
                final AbstractFlag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                for (final String entry : args[2].split(",")) {
                    if (!Permissions.hasPermission(player, "plots.set.flag." + args[1].toLowerCase() + "." + entry)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase() + "." + entry);
                        return false;
                    }
                }
                final String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                final Object parsed = af.parseValueRaw(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + af.getValueDesc());
                    return false;
                }
                Flag flag = FlagManager.getPlotFlag(plot, args[1].toLowerCase());
                if ((flag == null) || !flag.getAbstractFlag().isList()) {
                    flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), parsed);
                } else {
                    ((FlagValue.ListValue) flag.getAbstractFlag().value).add(flag.getValue(), value);
                }
                final boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                DBFunc.setFlags(plot, plot.getFlags().values());
                MainUtil.sendMessage(player, C.FLAG_ADDED);
                return true;
            }
            case "list": {
                if (!Permissions.hasPermission(player, "plots.flag.list")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.list");
                    return false;
                }
                if (args.length != 1) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag list");
                    return false;
                }
                final HashMap<String, ArrayList<String>> flags = new HashMap<>();
                for (final AbstractFlag af : FlagManager.getFlags()) {
                    final String type = af.value.getClass().getSimpleName().replaceAll("Value", "");
                    if (!flags.containsKey(type)) {
                        flags.put(type, new ArrayList<String>());
                    }
                    flags.get(type).add(af.getKey());
                }
                String message = "";
                String prefix = "";
                for (final String flag : flags.keySet()) {
                    message += prefix + "&6" + flag + ": &7" + StringMan.join(flags.get(flag), ", ");
                    prefix = "\n";
                }
                MainUtil.sendMessage(player, message);
                return true;
            }
        }
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
        return false;
    }
}
