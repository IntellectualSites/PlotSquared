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

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.StringComparsion;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PlotMain command class
 *
 * @author Citymonstret
 */
public class MainCommand implements CommandExecutor, TabCompleter {

    public static final String
            MAIN_PERMISSION = "plots.use";

    private static SubCommand[] _subCommands = new SubCommand[]{new Claim(), new Paste(), new Copy(), new Clipboard(), new Auto(), new Home(), new Visit(), new TP(), new Set(), new Clear(), new Delete(), new SetOwner(), new Denied(), new Helpers(), new Trusted(), new Info(), new list(), new Help(), new Debug(), new Schematic(), new plugin(), new Inventory(), new Purge(), new Reload(), new Merge(), new Unlink(), new Kick(), new Setup(), new DebugClaimTest(), new Inbox(), new Comment(), new Swap(), new MusicSubcommand()};

    public static ArrayList<SubCommand> subCommands = new ArrayList<SubCommand>() {
        {
            addAll(Arrays.asList(_subCommands));
        }
    };

    public static boolean no_permission(final Player player, final String permission) {
        PlayerFunctions.sendMessage(player, C.NO_PERMISSION, permission);
        return false;
    }

    public static List<SubCommand> getCommands(final SubCommand.CommandCategory category, final Player player) {
        final List<SubCommand> cmds = new ArrayList<>();
        for (final SubCommand c : subCommands) {
            if ((c.category.equals(category)) && c.permission.hasPermission(player)) {
                cmds.add(c);
            }
        }
        return cmds;
    }

    public static List<String> helpMenu(final Player player, final SubCommand.CommandCategory category, int page) {
        final List<SubCommand> commands = getCommands(category, player);
        // final int totalPages = ((int) Math.ceil(12 * (commands.size()) /
        // 100));
        final int perPage = 5;
        final int totalPages = (int) Math.ceil(commands.size() / perPage);
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
        help.add(C.HELP_CATEGORY.s()
                        .replace("%category%", category.toString())
                        .replace("%current%", "" + (page + 1))
                        .replace("%max%", "" + (totalPages + 1))
                        .replace("%dis%", "" + perPage)
                        .replace("%total%", "" + commands.size())
        );

        SubCommand cmd;

        final int start = page * perPage;
        for (int x = start; x < max; x++) {
            cmd = commands.get(x);
            String s = t(C.HELP_ITEM.s());
            s = s
                    .replace("%alias%", cmd.alias)
                    .replace("%usage%", cmd.usage.contains("plot") ? cmd.usage : "/plot " + cmd.usage)
                    .replace("%cmd%", cmd.cmd)
                    .replace("%desc%", cmd.description)
            ;
            help.add(s);
        }
        if (help.size() < 2) {
            help.add(t(C.NO_COMMANDS.s()));
        }
        return help;
    }

    private static String t(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        final Player player = (sender instanceof Player) ? (Player) sender : null;

        if (!PlotMain.hasPermission(player, MAIN_PERMISSION))
            return no_permission(player, MAIN_PERMISSION);

        if ((args.length < 1) || ((args.length >= 1) && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("he")))) {
            if (args.length < 2) {
                final StringBuilder builder = new StringBuilder();
                builder.append(C.HELP_INFO.s());
                for (final SubCommand.CommandCategory category : SubCommand.CommandCategory.values()) {
                    builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", category.toString().toLowerCase()).replaceAll("%category_desc%", category.toString()));
                }
                return PlayerFunctions.sendMessage(player, builder.toString());
            }
            final String cat = args[1];
            SubCommand.CommandCategory cato = null;
            for (final SubCommand.CommandCategory category : SubCommand.CommandCategory.values()) {
                if (cat.equalsIgnoreCase(category.toString())) {
                    cato = category;
                    break;
                }
            }
            if (cato == null) {
                final StringBuilder builder = new StringBuilder();
                builder.append(C.HELP_INFO.s());
                for (final SubCommand.CommandCategory category : SubCommand.CommandCategory.values()) {
                    builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", category.toString().toLowerCase()).replaceAll("%category_desc%", category.toString()));
                }
                return PlayerFunctions.sendMessage(player, builder.toString());
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', help.toString()));
            //return PlayerFunctions.sendMessage(player, help.toString());
        } else {
            for (final SubCommand command : subCommands) {
                if (command.cmd.equalsIgnoreCase(args[0]) || command.alias.equalsIgnoreCase(args[0])) {
                    final String[] arguments = new String[args.length - 1];
                    System.arraycopy(args, 1, arguments, 0, args.length - 1);
                    if (command.permission.hasPermission(player)) {
                        if ((player != null) || !command.isPlayer) {
                            return command.execute(player, arguments);
                        } else {
                            return !PlayerFunctions.sendMessage(null, C.IS_CONSOLE);
                        }
                    } else {
                        return no_permission(player, command.permission.permission.toLowerCase());
                    }
                }
            }
            PlayerFunctions.sendMessage(player, C.NOT_VALID_SUBCOMMAND);

            final String[] commands = new String[subCommands.size()];
            for (int x = 0; x < subCommands.size(); x++) {
                commands[x] = subCommands.get(x).cmd;
            }

            /* Let's try to get a proper usage string */
            String command = new StringComparsion(args[0], commands).getBestMatch();
            return PlayerFunctions.sendMessage(player, C.DID_YOU_MEAN, "/plot " + command);
            //PlayerFunctions.sendMessage(player, C.DID_YOU_MEAN, new StringComparsion(args[0], commands).getBestMatch());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        final Player player = (Player) commandSender;

        if (strings.length < 1) {
            if ((strings.length == 0) || "plots".startsWith(s)) {
                return Arrays.asList("plots");
            }
        }
        if (strings.length > 1) {
            return null;
        }
        if (!command.getLabel().equalsIgnoreCase("plots")) {
            return null;
        }
        final List<String> tabOptions = new ArrayList<>();
        final String arg = strings[0].toLowerCase();
        for (final SubCommand cmd : subCommands) {
            if (cmd.permission.hasPermission(player)) {
                if (cmd.cmd.startsWith(arg)) {
                    tabOptions.add(cmd.cmd);
                } else if (cmd.alias.startsWith(arg)) {
                    tabOptions.add(cmd.alias);
                }
            }
        }
        if (tabOptions.size() > 0) {
            return tabOptions;
        }
        return null;
    }
}
