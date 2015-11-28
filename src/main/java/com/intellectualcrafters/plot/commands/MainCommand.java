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
import java.util.HashSet;
import java.util.List;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.helpmenu.HelpMenu;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandHandlingOutput;
import com.plotsquared.general.commands.CommandManager;

/**
 * PlotSquared command class
 *
 */
public class MainCommand extends CommandManager<PlotPlayer> {
    
    private static MainCommand instance;
    private HashMap<String, Command<PlotPlayer>> setCommands;
    
    public static MainCommand getInstance() {
        if (instance == null) {
            instance = new MainCommand();
        }
        return instance;
    }
    
    private MainCommand() {
        super(null, new ArrayList<Command<PlotPlayer>>());
        instance = this;
        createCommand(new Buy());
        createCommand(new Save());
        createCommand(new Load());
        createCommand(new Unclaim());
        createCommand(new Confirm());
        createCommand(new Template());
        createCommand(new Download());
        createCommand(new Update());
        createCommand(new Template());
        createCommand(new Setup());
        createCommand(new DebugSaveTest());
        createCommand(new DebugLoadTest());
        createCommand(new CreateRoadSchematic());
        createCommand(new DebugAllowUnsafe());
        createCommand(new RegenAllRoads());
        createCommand(new Claim());
        createCommand(new Auto());
        createCommand(new Visit());
        createCommand(new Home());
        createCommand(new TP());
        createCommand(new Set());
        createCommand(new Toggle());
        createCommand(new Clear());
        createCommand(new Delete());
        createCommand(new Trust());
        createCommand(new Add());
        createCommand(new Deny());
        createCommand(new Untrust());
        createCommand(new Remove());
        createCommand(new Undeny());
        createCommand(new Info());
        createCommand(new list());
        createCommand(new Help());
        createCommand(new Debug());
        createCommand(new SchematicCmd());
        createCommand(new plugin());
        createCommand(new Purge());
        createCommand(new Reload());
        createCommand(new Merge());
        createCommand(new DebugPaste());
        createCommand(new Unlink());
        createCommand(new Kick());
        createCommand(new Rate());
        createCommand(new DebugClaimTest());
        createCommand(new Inbox());
        createCommand(new Comment());
        createCommand(new Database());
        createCommand(new Swap());
        createCommand(new MusicSubcommand());
        createCommand(new DebugRoadRegen());
        createCommand(new Trust());
        createCommand(new DebugExec());
        createCommand(new FlagCmd());
        createCommand(new Target());
        createCommand(new DebugFixFlags());
        createCommand(new Move());
        createCommand(new Condense());
        createCommand(new Condense());
        createCommand(new Copy());
        createCommand(new Chat());
        createCommand(new Trim());
        createCommand(new Done());
        createCommand(new Continue());
        createCommand(new BO3());
        // set commands
        createCommand(new Owner());
        createCommand(new Desc());
        createCommand(new Biome());
        createCommand(new Alias());
        createCommand(new SetHome());
        if (Settings.ENABLE_CLUSTERS) {
            MainCommand.getInstance().addCommand(new Cluster());
        }
    }

    public static boolean no_permission(final PlotPlayer player, final String permission) {
        MainUtil.sendMessage(player, C.NO_PERMISSION, permission);
        return false;
    }
    
    public static List<Command<PlotPlayer>> getCommandAndAliases(final CommandCategory category, final PlotPlayer player) {
        final List<Command<PlotPlayer>> commands = new ArrayList<>();
        for (final Command<PlotPlayer> command : getInstance().getCommands()) {
            if ((category != null) && !command.getCategory().equals(category)) {
                continue;
            }
            if ((player != null) && !Permissions.hasPermission(player, command.getPermission())) {
                continue;
            }
            commands.add(command);
        }
        return commands;
    }
    
    public static List<Command<PlotPlayer>> getCommands(final CommandCategory category, final PlotPlayer player) {
        final List<Command<PlotPlayer>> commands = new ArrayList<>();
        for (final Command<PlotPlayer> command : new HashSet<>(getInstance().getCommands())) {
            if ((category != null) && !command.getCategory().equals(category)) {
                continue;
            }
            if ((player != null) && !Permissions.hasPermission(player, command.getPermission())) {
                continue;
            }
            commands.add(command);
        }
        return commands;
    }
    
    public static void displayHelp(final PlotPlayer player, String cat, int page, final String label) {
        CommandCategory catEnum = null;
        if (cat != null) {
            if (StringMan.isEqualIgnoreCase(cat, "all")) {
                catEnum = null;
            } else {
                for (final CommandCategory c : CommandCategory.values()) {
                    if (StringMan.isEqualIgnoreCaseToAny(cat, c.name(), c.toString())) {
                        catEnum = c;
                        cat = c.name();
                        break;
                    }
                }
                if (catEnum == null) {
                    cat = null;
                }
            }
        }
        if ((cat == null) && (page == 0)) {
            final StringBuilder builder = new StringBuilder();
            builder.append(C.HELP_HEADER.s());
            for (final CommandCategory c : CommandCategory.values()) {
                builder.append("\n" + StringMan.replaceAll(C.HELP_INFO_ITEM.s(), "%category%", c.toString().toLowerCase(), "%category_desc%", c.toString()));
            }
            builder.append("\n").append(C.HELP_INFO_ITEM.s().replaceAll("%category%", "all").replaceAll("%category_desc%", "Display all commands"));
            builder.append("\n" + C.HELP_FOOTER.s());
            MainUtil.sendMessage(player, builder.toString(), false);
            return;
        }
        page--;
        new HelpMenu(player).setCategory(catEnum).getCommands().generateMaxPages().generatePage(page, label).render();
    }
    
    public static boolean onCommand(final PlotPlayer player, final String cmd, String... args) {
        int help_index = -1;
        String category = null;
        if (args.length == 0) {
            help_index = 0;
        } else if (StringMan.isEqualIgnoreCaseToAny(args[0], "he", "help", "?")) {
            help_index = 0;
            switch (args.length) {
                case 3: {
                    category = args[1];
                    if (MathMan.isInteger(args[2])) {
                        try {
                            help_index = Integer.parseInt(args[2]);
                        } catch (final NumberFormatException e) {
                            help_index = 1;
                        }
                    }
                    break;
                }
                case 2: {
                    if (MathMan.isInteger(args[1])) {
                        category = null;
                        try {
                            help_index = Integer.parseInt(args[1]);
                        } catch (final NumberFormatException e) {
                            help_index = 1;
                        }
                    } else {
                        help_index = 1;
                        category = args[1];
                    }
                    break;
                }
            }
        } else if ((args.length == 1) && MathMan.isInteger(args[args.length - 1])) {
            try {
                help_index = Integer.parseInt(args[args.length - 1]);
            } catch (final NumberFormatException e) {}
        } else if (ConsolePlayer.isConsole(player) && (args.length >= 2)) {
            final String[] split = args[0].split(";");
            String world;
            PlotId id;
            if (split.length == 2) {
                world = player.getLocation().getWorld();
                id = PlotId.fromString(split[0] + ";" + split[1]);
            } else if (split.length == 3) {
                world = split[0];
                id = PlotId.fromString(split[1] + ";" + split[2]);
            } else {
                id = null;
                world = null;
            }
            if ((id != null) && PS.get().isPlotWorld(world)) {
                final Plot plot = MainUtil.getPlotAbs(world, id);
                if (plot != null) {
                    player.teleport(plot.getBottomAbs());
                    args = Arrays.copyOfRange(args, 1, args.length);
                }
            }
            
        }
        if (help_index != -1) {
            displayHelp(player, category, help_index, cmd);
            return true;
        }
        if (args[0].contains(":")) {
            args[0] = args[0].replaceFirst(":", " ");
        }
        String fullCmd = StringMan.join(args, " ");
        getInstance().handle(player, cmd + " " + fullCmd);
        return true;
    }
    
    public int getMatch(String[] args, Command<PlotPlayer> cmd) {
        int count = 0;
        String perm = cmd.getPermission();
        HashSet<String> desc = new HashSet<String>();
        for (String alias : cmd.getAliases()) {
            if (alias.startsWith(args[0])) {
                count += 5;
            }
        }
        for (String word : cmd.getDescription().split(" ")) {
            desc.add(word);
        }
        for (String arg : args) {
            if (perm.startsWith(arg)) {
                count++;
            }
            if (desc.contains(arg)) {
                count++;
            }
        }
        String[] usage = cmd.getUsage().split(" ");
        for (int i = 0; i < Math.min(4 , usage.length); i++) {
            int require;
            if (usage[i].startsWith("<")) {
                require = 1;
            } else {
                require = 0;
            }
            String[] split = usage[i].split("\\|| |\\>|\\<|\\[|\\]|\\{|\\}|\\_|\\/");
            for (int j = 0; j < split.length; j++) {
                for (String arg : args) {
                    if (StringMan.isEqualIgnoreCase(arg, split[j])) {
                        count += 5 - i + require;
                    }
                }
            }
        }
        count += StringMan.intersection(desc, args);
        return count;
    }
    
    @Override
    public int handle(final PlotPlayer plr, final String input) {
        final String[] parts = input.split(" ");
        String[] args;
        String label;
        if (parts.length == 1) {
            label = null;
            args = new String[0];
        } else {
            label = parts[1];
            args = new String[parts.length - 2];
            System.arraycopy(parts, 2, args, 0, args.length);
        }
        Command<PlotPlayer> cmd;
        if (label != null) {
            cmd = getInstance().commands.get(label.toLowerCase());
        } else {
            cmd = null;
        }
        if (cmd == null) {
            MainUtil.sendMessage(plr, C.NOT_VALID_SUBCOMMAND);
            {
                final List<Command<PlotPlayer>> cmds = getCommands(null, plr);
                if ((label == null) || (cmds.size() == 0)) {
                    MainUtil.sendMessage(plr, C.DID_YOU_MEAN, "/plot help");
                } else {
                    final HashSet<String> setargs = new HashSet<>(args.length + 1);
                    for (final String arg : args) {
                        setargs.add(arg.toLowerCase());
                    }
                    setargs.add(label);
                    final String[] allargs = setargs.toArray(new String[setargs.size()]);
                    int best = 0;
                    for (final Command<PlotPlayer> current : cmds) {
                        int match = getMatch(allargs, current);
                        if (match > best) {
                            cmd = current;
                        }
                    }
                    if (cmd == null) {
                        cmd = new StringComparison<>(label, getCommandAndAliases(null, plr)).getMatchObject();
                    }
                    MainUtil.sendMessage(plr, C.DID_YOU_MEAN, cmd.getUsage().replaceAll("\\{label\\}", parts[0]));
                }
            }
            return CommandHandlingOutput.NOT_FOUND;
        }
        if (!cmd.getRequiredType().allows(plr)) {
            if (ConsolePlayer.isConsole(plr)) {
                MainUtil.sendMessage(plr, C.IS_CONSOLE);
            } else {
                MainUtil.sendMessage(plr, C.NOT_CONSOLE);
            }
            return CommandHandlingOutput.CALLER_OF_WRONG_TYPE;
        }
        if (!Permissions.hasPermission(plr, cmd.getPermission())) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, cmd.getPermission());
            return CommandHandlingOutput.NOT_PERMITTED;
        }
        final Argument<?>[] requiredArguments = cmd.getRequiredArguments();
        if ((requiredArguments != null) && (requiredArguments.length > 0)) {
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
            final boolean result = cmd.onCommand(plr, args);
            if (!result) {
                cmd.getUsage();
                // Unecessary!
                //                if (usage != null && !usage.isEmpty()) {
                //                    MainUtil.sendMessage(plr, usage);
                //                }
                return CommandHandlingOutput.WRONG_USAGE;
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            return CommandHandlingOutput.ERROR;
        }
        return CommandHandlingOutput.SUCCESS;
    }
}
