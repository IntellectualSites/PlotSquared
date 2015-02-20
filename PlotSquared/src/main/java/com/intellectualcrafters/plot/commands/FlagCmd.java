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

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.bukkit.PlayerFunctions;

public class FlagCmd extends SubCommand {

    public FlagCmd() {
        super(Command.FLAG, "Manage plot flags", "f", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player player, final String... args) {
        /*
         *  plot flag set fly true
         *  plot flag remove fly
         *  plot flag remove use 1,3
         *  plot flag add use 2,4
         *  plot flag list
         */
        if (args.length == 0) {
            PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
            return false;
        }
        Plot plot = PlayerFunctions.getCurrentPlot(player);
        if (plot == null) {
            PlayerFunctions.sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.hasRights(player) && !BukkitMain.hasPermission(player, "plots.set.flag.other")) {
            PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.set.flag.other");
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "info": {
                if (!BukkitMain.hasPermission(player, "plots.set.flag")) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.flag.info");
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                AbstractFlag af = FlagManager.getFlag(args[1]);
                if (af == null) {
                    PlayerFunctions.sendMessage(player, C.NOT_VALID_FLAG);
                    PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                // flag key
                PlayerFunctions.sendMessage(player, C.FLAG_KEY, af.getKey());
                // flag type
                PlayerFunctions.sendMessage(player, C.FLAG_TYPE, af.value.getClass().getSimpleName());
                // Flag type description
                PlayerFunctions.sendMessage(player, C.FLAG_DESC, af.getValueDesc());
                PlayerFunctions.sendMessage(player, "&cNot implemented.");
            }
            case "set": {
                if (!BukkitMain.hasPermission(player, "plots.set.flag")) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.set.flag");
                    return false;
                }
                if (args.length < 3) {
                    PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag set <flag> <value>");
                    return false;
                }
                AbstractFlag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    PlayerFunctions.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                if (!BukkitMain.hasPermission(player, "plots.set.flag." + args[1].toLowerCase())) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase());
                    return false;
                }
                String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                Object parsed = af.parseValueRaw(value);
                if (parsed == null) {
                    PlayerFunctions.sendMessage(player, "&c" + af.getValueDesc());
                    return false;
                }
                final Flag flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), parsed);
                boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    PlayerFunctions.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                PlayerFunctions.sendMessage(player, C.FLAG_ADDED);
                PlotListener.plotEntry(player, plot);
                return true;
            }
            case "remove": {
                if (!BukkitMain.hasPermission(player, "plots.flag.remove")) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.flag.remove");
                    return false;
                }
                if (args.length != 2 && args.length != 3) {
                    PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag remove <flag> [values]");
                    return false;
                }
                AbstractFlag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    PlayerFunctions.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                if (!BukkitMain.hasPermission(player, "plots.set.flag." + args[1].toLowerCase())) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase());
                    return false;
                }
                Flag flag = FlagManager.getPlotFlagAbs(plot, args[1].toLowerCase());
                if (flag == null) {
                    PlayerFunctions.sendMessage(player, C.FLAG_NOT_IN_PLOT);
                    return false;
                }
                if (args.length == 3 && flag.getAbstractFlag().isList()) {
                    String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                    ((FlagValue.ListValue) flag.getAbstractFlag().value).remove(flag.getValue(), value);
                    DBFunc.setFlags(plot.world, plot, plot.settings.flags);
                }
                else {
                    boolean result = FlagManager.removePlotFlag(plot, flag.getKey());
                    if (!result) {
                        PlayerFunctions.sendMessage(player, C.FLAG_NOT_REMOVED);
                        return false;
                    }
                }
                PlayerFunctions.sendMessage(player, C.FLAG_REMOVED);
                PlotListener.plotEntry(player, plot);
                return true;
            }
            case "add": {
                if (!BukkitMain.hasPermission(player, "plots.flag.add")) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.flag.add");
                    return false;
                }
                if (args.length < 3) {
                    PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
                    return false;
                }
                AbstractFlag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    PlayerFunctions.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                if (!BukkitMain.hasPermission(player, "plots.set.flag." + args[1].toLowerCase())) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase());
                    return false;
                }
                String value = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                Object parsed = af.parseValueRaw(value);
                if (parsed == null) {
                    PlayerFunctions.sendMessage(player, "&c" + af.getValueDesc());
                    return false;
                }
                Flag flag = FlagManager.getPlotFlag(plot, args[1].toLowerCase());
                if (flag == null || !flag.getAbstractFlag().isList()) {
                    flag = new Flag(FlagManager.getFlag(args[1].toLowerCase(), true), parsed);
                }
                else {
                    ((FlagValue.ListValue) flag.getAbstractFlag().value).add(flag.getValue(), value);
                }
                boolean result = FlagManager.addPlotFlag(plot, flag);
                if (!result) {
                    PlayerFunctions.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                DBFunc.setFlags(plot.world, plot, plot.settings.flags);
                PlayerFunctions.sendMessage(player, C.FLAG_ADDED);
                PlotListener.plotEntry(player, plot);
                return true;
            }
            case "list": {
                if (!BukkitMain.hasPermission(player, "plots.flag.list")) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.flag.list");
                    return false;
                }
                if (args.length != 1) {
                    PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag list");
                    return false;
                }
                HashMap<String, ArrayList<String>> flags = new HashMap<>();
                for (AbstractFlag af : FlagManager.getFlags()) {
                    String type = af.value.getClass().getSimpleName().replaceAll("Value", "");
                    if (!flags.containsKey(type)) {
                        flags.put(type, new ArrayList<String>());
                    }
                    flags.get(type).add(af.getKey());
                }
                String message = "";
                String prefix = "";
                for (String flag : flags.keySet()) {
                    message += prefix + "&6" + flag +": &7" + StringUtils.join(flags.get(flag), ", ");
                    prefix = "\n";
                }
                PlayerFunctions.sendMessage(player, message);
                return true;
            }
        }
        PlayerFunctions.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
        return false;
    }
}
