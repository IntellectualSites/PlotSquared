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
import java.util.Iterator;
import java.util.List;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.callers.ConsoleCaller;
import com.intellectualcrafters.plot.commands.callers.PlotPlayerCaller;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandHandlingOutput;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.callers.CommandCaller;
import com.intellectualsites.commands.util.StringUtil;

/**
 * PlotSquared command class
 *
 * @author Citymonstret
 */
public class MainCommand extends CommandManager {

    public static MainCommand instance = new MainCommand();

    private MainCommand() {
        super(null, new ArrayList<Command>());
        List<SubCommand> toAdd = Arrays.asList(
                new Buy(), new Save(), new Load(),
                new Template(), new Download(),
                new Update(), new Template(),
                new Setup(), new DebugUUID(),
                new DebugFill(), new DebugSaveTest(),
                new DebugLoadTest(), new CreateRoadSchematic(),
                new DebugAllowUnsafe(), new RegenAllRoads(),
                new DebugClear(), new Claim(),
                new Auto(), new Home(), new Visit(),
                new TP(), new Set(), new Toggle(),
                new Clear(), new Delete(), new SetOwner(),
                new Trust(), new Add(), new Deny(),
                new Untrust(), new Remove(), new Undeny(),
                new Info(), new list(), new Help(),
                new Debug(), new SchematicCmd(), new plugin(),
                new Purge(), new Reload(), new Merge(),
                new DebugPaste(), new Unlink(), new Kick(),
                new Rate(), new DebugClaimTest(), new Inbox(),
                new Comment(), new Database(), new Swap(),
                new MusicSubcommand(), new DebugRoadRegen(),
                new Trust(), new DebugExec(), new FlagCmd(),
                new Target(), new DebugFixFlags(), new Move(),
                new Condense(), new Condense(), new Copy(),
                new Chat());
        if (Settings.ENABLE_CLUSTERS) {
            toAdd.add(new Cluster());
        }
        for (final SubCommand cmd : toAdd) {
            if (!createCommand(cmd)) {
                PS.log("Failed to create command: " + cmd.getClass());
            }
        }
    }

    public static boolean no_permission(final PlotPlayer player, final String permission) {
        MainUtil.sendMessage(player, C.NO_PERMISSION, permission);
        return false;
    }
    
    public static List<Command> getCommands(final CommandCategory category, final PlotPlayer player) {
        final List<Command> cmds = new ArrayList<>();
        for (final Command c : instance.commands) {
            if (!c.getRequiredType().equals(PlotPlayer.class)) {
                if ((c.getCategory().equals(category)) && player.hasPermission(c.getPermission())) {
                    cmds.add(c);
                }
            }
        }
        return cmds;
    }
    
    public static List<String> helpMenu(final PlotPlayer player, final CommandCategory category, int page) {
        List<Command> commands;
        if (category != null) {
            commands = getCommands(category, player);
        } else {
            commands = instance.commands;
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
        help.add(C.HELP_CATEGORY.s().replace("%category%", category == null ? "All" : category.toString()).replace("%current%", "" + (page + 1)).replace("%max%", "" + (totalPages)).replace("%dis%", "" + perPage).replace("%total%", "" + commands.size()));
        Command cmd;
        final int start = page * perPage;
        for (int x = start; x < max; x++) {
            cmd = commands.get(x);
            String s = C.HELP_ITEM.s();
            if (cmd.getAliases().length > 0) {
                s = s.replace("%alias%", cmd.getAliases()[0]);
            }
            else {
                s = s.replace("%alias%", "");
            }
            s = s.replace("%usage%", cmd.getUsage().contains("plot") ? cmd.getUsage() : "/plot " + cmd.getUsage()).replace("%cmd%", cmd.getCommand()).replace("%desc%", cmd.getDescription()).replace("[]", "");
            help.add(s);
        }
        if (help.size() < 2) {
            help.add(C.NO_COMMANDS.s());
        }
        return help;
    }
    
    public static boolean onCommand(final PlotPlayer player, final String cmd, final String... args) {
        if ((args.length < 1) || ((args.length >= 1) && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("he")))) {
            if (args.length < 2) {
                final StringBuilder builder = new StringBuilder();
                builder.append(C.HELP_INFO.s());
                for (final CommandCategory category : CommandCategory.values()) {
                    builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", category.toString().toLowerCase()).replaceAll("%category_desc%", category.toString()));
                }
                builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", "all").replaceAll("%category_desc%", "Display all commands"));
                return MainUtil.sendMessage(player, builder.toString());
            }
            final String cat = args[1];
            CommandCategory cato = null;
            for (final CommandCategory category : CommandCategory.values()) {
                if (cat.equalsIgnoreCase(category.toString())) {
                    cato = category;
                    break;
                }
            }
            if ((cato == null) && !cat.equalsIgnoreCase("all")) {
                final StringBuilder builder = new StringBuilder();
                builder.append(C.HELP_INFO.s());
                for (final CommandCategory category : CommandCategory.values()) {
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
            CommandCaller caller;
            if (player != null) {
                caller = new PlotPlayerCaller(player);
            } else {
                caller = new ConsoleCaller();
            }
            StringBuilder builder = new StringBuilder(cmd).append(" ");
            Iterator<String> iterator = Arrays.asList(args).iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            instance.handle(caller, builder.toString());
            // for (final SubCommand command : subCommands) {
            //    if (command.cmd.equalsIgnoreCase(args[0]) || command.alias.contains(args[0].toLowerCase())) {
            //        final String[] arguments = new String[args.length - 1];
            //        System.arraycopy(args, 1, arguments, 0, args.length - 1);
            //        if (command.permission.hasPermissipon(player)) {
            //            if ((player != null) || !command.isPlayer) {
            //                return command.execute(player, arguments);
            //            } else {
            //                return !MainUtil.sendMessage(null, C.IS_CONSOLE);
            //            }
            //        } else {
            //            return no_permission(player, command.permission.permission.toLowerCase());
            //        }
            //    }
            // }
            // MainUtil.sendMessage(player, C.NOT_VALID_SUBCOMMAND);
            // final String[] commands = new String[subCommands.size()];
            // for (int x = 0; x < subCommands.size(); x++) {
            //    commands[x] = subCommands.get(x).cmd;
            // }
            // /* Let's try to get a proper usage string */
            // final String command = new StringComparison(args[0], commands).getBestMatch();
            // return MainUtil.sendMessage(player, C.DID_YOU_MEAN, "/plot " + command);
            // PlayerFunctions.sendMessage(player, C.DID_YOU_MEAN, new
            // StringComparsion(args[0], commands).getBestMatch());
        }
        return true;
    }

    @Override
    public int handle(CommandCaller caller, String input) {
        String[] parts = input.split(" ");
        String[] args;
        String command = parts[0];
        if (parts.length == 1) {
            args = new String[0];
        } else {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }
        Command cmd = null;
        for (Command c1 : this.commands) {
            if (c1.getCommand().equalsIgnoreCase(command) || StringUtil.inArray(command, c1.getAliases(), false)) {
                cmd = c1;
                break;
            }
        }
        if (cmd == null) {
            caller.message(C.NOT_VALID_SUBCOMMAND);
            {
                final String[] commands = new String[this.commands.size()];
                for (int i = 0; i < commands.length; i++) {
                    commands[i] = this.commands.get(i).getCommand();
                }
                final String bestMatch = new StringComparison(args[0], commands).getBestMatch();
                caller.message(C.DID_YOU_MEAN, "/plot " + bestMatch);
            }
            return CommandHandlingOutput.NOT_FOUND;
        }
        if (!cmd.getRequiredType().isInstance(caller.getSuperCaller())) {
            if (caller instanceof PlotPlayerCaller) {
                caller.message(C.NOT_CONSOLE);
            } else {
                caller.message(C.IS_CONSOLE);
                return CommandHandlingOutput.CALLER_OF_WRONG_TYPE;
            }
        }
        if (!caller.hasPermission(cmd.getPermission())) {
            caller.message(C.NO_PERMISSION, cmd.getPermission());
            return CommandHandlingOutput.NOT_PERMITTED;
        }
        Argument[] requiredArguments = cmd.getRequiredArguments();
        if (requiredArguments != null && requiredArguments.length > 0) {
            boolean success = true;
            if (args.length < requiredArguments.length) {
                success = false;
            } else {
                for (int i = 0; i < requiredArguments.length; i++) {
                    if (requiredArguments[i].parse(args[i]) == null) {
                        success = false;
                        break;
                    }
                }
            }
            if (!success) {
                caller.sendRequiredArgumentsList(this, cmd, requiredArguments);
                return CommandHandlingOutput.WRONG_USAGE;
            }
        }
        try {
            boolean a = cmd.onCommand(caller, args);
            if (!a) {
                String usage = cmd.getUsage();
                if (usage != null && !usage.isEmpty()) {
                    caller.message(usage);
                }
                return CommandHandlingOutput.WRONG_USAGE;
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            return CommandHandlingOutput.ERROR;
        }
        return CommandHandlingOutput.SUCCESS;
    }
}
