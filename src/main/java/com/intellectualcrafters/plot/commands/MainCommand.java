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
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandHandlingOutput;
import com.intellectualsites.commands.CommandManager;
import com.intellectualcrafters.plot.object.PlotPlayer;

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
    
    public static ArrayList<Command> getCommands(final CommandCategory category, final PlotPlayer player) {
        ArrayList<Command> cmds = instance.getCommands();
        for (Iterator<Command> iter = cmds.iterator(); iter.hasNext();){
            Command cmd = iter.next();
            if ((category != null && (cmd.getCategory().equals(category))) || !player.hasPermission(cmd.getPermission())) {
                iter.remove();
            }
        }
        return cmds;
    }
    
    public static List<String> helpMenu(final PlotPlayer player, final CommandCategory category, int page) {
        List<Command> commands;
        commands = getCommands(category, player);
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
            if (cmd.getAliases().size() > 0) {
                s = s.replace("%alias%", StringMan.join(cmd.getAliases(), "|"));
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
    
    public static void displayHelp(PlotPlayer player, String cat, int page) {
        if (cat != null && StringMan.isEqualIgnoreCase(cat, "all")) {
            cat = null;
        }
        if (cat == null && page == 0) {
            final StringBuilder builder = new StringBuilder();
            builder.append(C.HELP_INFO.s());
            for (final CommandCategory category : CommandCategory.values()) {
                builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", category.toString().toLowerCase()).replaceAll("%category_desc%", category.toString()));
            }
            builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", "all").replaceAll("%category_desc%", "Display all commands"));
            MainUtil.sendMessage(player, builder.toString());
            return;
        }
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
            MainUtil.sendMessage(player, builder.toString(), false);
            return;
        }
        final StringBuilder help = new StringBuilder();
        for (final String string : helpMenu(player, cato, page)) {
            help.append(string).append("\n");
        }
        MainUtil.sendMessage(player, help.toString());
    }
    
    public static boolean onCommand(final PlotPlayer player, final String cmd, final String... args) {
        int help_index = -1;
        String category = null;
        if (args.length == 0) {
            help_index = 0;
        }
        else if (StringMan.isEqualIgnoreCaseToAny(args[0], "he", "help", "?")) {
            help_index = 0;
            switch (args.length) {
                case 3: {
                    category = args[1];
                }
                case 2: {
                    if (MathMan.isInteger(args[args.length - 1])) {
                        category = null;
                        try {
                            help_index = Integer.parseInt(args[1]) - 1;
                        }
                        catch (NumberFormatException e) {}
                    }
                    if (category == null) {
                        category = args[1];
                    }
                }
                case 1: {
                    break;
                }
            }
            if (args.length == 3) {
                if (MathMan.isInteger(args[args.length - 1])) {
                    category = null;
                    try {
                        help_index = Integer.parseInt(args[1]) - 1;
                    }
                    catch (NumberFormatException e) {}
                }
                else {
                    category = args[1];
                }
            }
        }
        else if (MathMan.isInteger(args[args.length - 1])) {
            try {
                help_index = Integer.parseInt(args[args.length - 1]) - 1;
            }
            catch (NumberFormatException e) {}
        }
        if (help_index != -1) {
            displayHelp(player, category, help_index);
            return true;
        }
        PlotPlayer caller;
        StringBuilder builder = new StringBuilder(cmd).append(" ");
        Iterator<String> iterator = Arrays.asList(args).iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(" ");
            }
        }
        instance.handle(player, builder.toString());
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
        return true;
    }

    @Override
    public int handle(PlotPlayer plr, String input) {
        String[] parts = input.split(" ");
        String[] args;
        String command = parts[0].toLowerCase();
        if (parts.length == 1) {
            args = new String[0];
        } else {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }
        Command cmd = null;
        System.out.print(command);
        System.out.print(StringMan.join(commands.entrySet(), ", "));
        cmd = this.commands.get(command);
        if (cmd == null) {
            MainUtil.sendMessage(plr, C.NOT_VALID_SUBCOMMAND);
            {
                final String[] commands = new String[this.commands.size()];
                for (int i = 0; i < commands.length; i++) {
                    commands[i] = this.commands.get(i).getCommand();
                }
                final String bestMatch = new StringComparison<String>(args[0], commands).getBestMatch();
                MainUtil.sendMessage(plr, C.DID_YOU_MEAN, "/plot " + bestMatch);
            }
            return CommandHandlingOutput.NOT_FOUND;
        }
        if (cmd.getRequiredType().allows(plr)) {
            if (ConsolePlayer.isConsole(plr)) {
                MainUtil.sendMessage(plr, C.IS_CONSOLE);
            } else {
                MainUtil.sendMessage(plr, C.NOT_CONSOLE);
            }
            return CommandHandlingOutput.CALLER_OF_WRONG_TYPE;
        }
        if (!plr.hasPermission(cmd.getPermission())) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, cmd.getPermission());
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
                C.COMMAND_SYNTAX.send(plr, cmd.getUsage());
                return CommandHandlingOutput.WRONG_USAGE;
            }
        }
        try {
            boolean result = cmd.onCommand(plr, args);
            if (!result) {
                String usage = cmd.getUsage();
                if (usage != null && !usage.isEmpty()) {
                    MainUtil.sendMessage(plr, usage);
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
