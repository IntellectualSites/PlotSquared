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

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;

/**
 * PlotSquared command class
 *
 * @author Citymonstret
 */
public class MainCommand {
    /**
     * Main Permission Node
     */
    private final static SubCommand[] _subCommands = new SubCommand[] {  };
    public final static ArrayList<SubCommand> subCommands = new ArrayList<SubCommand>() {
        {
            addAll(Arrays.asList(_subCommands));
        }
    };
    
    public static boolean no_permission(final PlotPlayer player, final String permission) {
        MainUtil.sendMessage(player, C.NO_PERMISSION, permission);
        return false;
    }
    
    public static List<SubCommand> getCommands(final SubCommand.CommandCategory category, final PlotPlayer player) {
        final List<SubCommand> cmds = new ArrayList<>();
        for (final SubCommand c : subCommands) {
            if (!c.isPlayer || (player != null)) {
                if ((c.category.equals(category)) && c.permission.hasPermission(player)) {
                    cmds.add(c);
                }
            }
        }
        return cmds;
    }
    
    public static List<String> helpMenu(final PlotPlayer player, final SubCommand.CommandCategory category, int page) {
        List<SubCommand> commands;
        if (category != null) {
            commands = getCommands(category, player);
        } else {
            commands = subCommands;
        }
        // final int totalPages = ((int) Math.ceil(12 * (commands.size()) /
        // 100));
        final int perPage = 5;
        final int totalPages = (commands.size() / perPage) + (commands.size() % perPage == 0 ? 0 : 1);
        if (page > totalPages) {
            page = totalPages;
        }
        int max = (page * perPage) + perPage;
        if (max > commands.size()) {
            max = commands.size();
        }
        final List<String> help = new ArrayList<>();
        help.add(C.HELP_HEADER.s());
        // HELP_CATEGORY("&cCategory: &6%category%&c, Page: %current%&c/&6%max%&c, Displaying: &6%dis%&c/&6%total%"),
        help.add(C.HELP_CATEGORY.s().replace("%category%", category == null ? "All" : category.toString()).replace("%current%", "" + (page + 1)).replace("%max%", "" + (totalPages)).replace("%dis%", "" + (commands.size() % perPage)).replace("%total%", "" + commands.size()));
        SubCommand cmd;
        final int start = page * perPage;
        for (int x = start; x < max; x++) {
            cmd = commands.get(x);
            String s = t(C.HELP_ITEM.s());
            if (cmd.alias.size() > 0) {
                s = s.replace("%alias%", cmd.alias.get(0));
            }
            else {
                s = s.replace("%alias%", "");
            }
            s = s.replace("%usage%", cmd.usage.contains("plot") ? cmd.usage : "/plot " + cmd.usage).replace("%cmd%", cmd.cmd).replace("%desc%", cmd.description).replace("[]", "");
            help.add(s);
        }
        if (help.size() < 2) {
            help.add(t(C.NO_COMMANDS.s()));
        }
        return help;
    }

    private static String t(final String s) {
        return MainUtil.colorise('&', s);
    }
    
    public static boolean onCommand(final PlotPlayer player, final String cmd, final String... args) {
        if (!Permissions.hasPermission(player, PlotSquared.MAIN_PERMISSION)) {
            return no_permission(player, PlotSquared.MAIN_PERMISSION);
        }
        if ((args.length < 1) || ((args.length >= 1) && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("he")))) {
            if (args.length < 2) {
                final StringBuilder builder = new StringBuilder();
                builder.append(C.HELP_INFO.s());
                for (final SubCommand.CommandCategory category : SubCommand.CommandCategory.values()) {
                    builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", category.toString().toLowerCase()).replaceAll("%category_desc%", category.toString()));
                }
                builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", "all").replaceAll("%category_desc%", "Display all commands"));
                return MainUtil.sendMessage(player, builder.toString());
            }
            final String cat = args[1];
            SubCommand.CommandCategory cato = null;
            for (final SubCommand.CommandCategory category : SubCommand.CommandCategory.values()) {
                if (cat.equalsIgnoreCase(category.toString())) {
                    cato = category;
                    break;
                }
            }
            if ((cato == null) && !cat.equalsIgnoreCase("all")) {
                final StringBuilder builder = new StringBuilder();
                builder.append(C.HELP_INFO.s());
                for (final SubCommand.CommandCategory category : SubCommand.CommandCategory.values()) {
                    builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", category.toString().toLowerCase()).replaceAll("%category_desc%", category.toString()));
                }
                return MainUtil.sendMessage(player, builder.toString(), false);
            }
            final StringBuilder help = new StringBuilder();
            int page = 0;
            boolean digit = true;
            String arg2;
            if (args.length > 2) {
                arg2 = args[2];
            } else {
                arg2 = "1";
            }
            for (final char c : arg2.toCharArray()) {
                if (!Character.isDigit(c)) {
                    digit = false;
                    break;
                }
            }
            if (digit) {
                page = Integer.parseInt(arg2);
                if (--page < 0) {
                    page = 0;
                }
            }
            for (final String string : helpMenu(player, cato, page)) {
                help.append(string).append("\n");
            }
            MainUtil.sendMessage(player, help.toString());
            // return PlayerFunctions.sendMessage(player, help.toString());
        } else {
            for (final SubCommand command : subCommands) {
                if (command.cmd.equalsIgnoreCase(args[0]) || command.alias.contains(args[0].toLowerCase())) {
                    final String[] arguments = new String[args.length - 1];
                    System.arraycopy(args, 1, arguments, 0, args.length - 1);
                    if (command.permission.hasPermission(player)) {
                        if ((player != null) || !command.isPlayer) {
                            return command.execute(player, arguments);
                        } else {
                            return !MainUtil.sendMessage(null, C.IS_CONSOLE);
                        }
                    } else {
                        return no_permission(player, command.permission.permission.toLowerCase());
                    }
                }
            }
            MainUtil.sendMessage(player, C.NOT_VALID_SUBCOMMAND);
            final String[] commands = new String[subCommands.size()];
            for (int x = 0; x < subCommands.size(); x++) {
                commands[x] = subCommands.get(x).cmd;
            }
            /* Let's try to get a proper usage string */
            final String command = new StringComparison(args[0], commands).getBestMatch();
            return MainUtil.sendMessage(player, C.DID_YOU_MEAN, "/plot " + command);
            // PlayerFunctions.sendMessage(player, C.DID_YOU_MEAN, new
            // StringComparsion(args[0], commands).getBestMatch());
        }
        return true;
    }
}
