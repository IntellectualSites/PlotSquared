package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.plotsquared.bukkit.commands.DebugUUID;
import com.plotsquared.general.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created 2015-02-20 for PlotSquared
 *

 */
public class BukkitCommand implements CommandExecutor, TabCompleter {

    public BukkitCommand() {
        MainCommand.getInstance().addCommand(new DebugUUID());
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final org.bukkit.command.Command command, final String commandLabel,
            final String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.getPlayer((Player) commandSender), commandLabel, args);
        }
        if (commandSender == null || commandSender.getClass() == Bukkit.getConsoleSender().getClass()) {
            return MainCommand.onCommand(ConsolePlayer.getConsole(), commandLabel, args);
        }
        @SuppressWarnings("deprecation")
        ConsolePlayer sender = new ConsolePlayer() {
            @Override
            public void sendMessage(String message) {
                commandSender.sendMessage(commandLabel);
            }

            @Override
            public boolean hasPermission(String perm) {
                return commandSender.hasPermission(commandLabel);
            }

            @Override
            public String getName() {
                if (commandSender.getName().equals("CONSOLE")) {
                    return "*";
                }
                return commandSender.getName();
            }
        };
        sender.teleport(ConsolePlayer.getConsole().getLocationFull());
        boolean result = MainCommand.onCommand(sender, commandLabel, args);
        ConsolePlayer.getConsole().teleport(sender.getLocationFull());
        return result;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final org.bukkit.command.Command command, final String s,
            final String[] strings) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        final PlotPlayer player = BukkitUtil.getPlayer((Player) commandSender);
        if (strings.length < 1) {
            if ((strings.length == 0) || "plots".startsWith(s)) {
                return Collections.singletonList("plots");
            }
        }
        if (strings.length > 1) {
            return null;
        }
        final Set<String> tabOptions = new HashSet<>();
        final String arg = strings[0].toLowerCase();
        ArrayList<String> labels = new ArrayList<>();
        for (final Command<PlotPlayer> cmd : MainCommand.getInstance().getCommands()) {
            final String label = cmd.getCommand();
            HashSet<String> aliases = new HashSet<>(cmd.getAliases());
            aliases.add(label);
            for (String alias : aliases) {
                labels.add(alias);
                if (alias.startsWith(arg)) {
                    if (Permissions.hasPermission(player, cmd.getPermission())) {
                        tabOptions.add(label);
                    } else {
                        break;
                    }
                }
            }
        }
        String best = new StringComparison<>(arg, labels).getBestMatch();
        tabOptions.add(best);
        if (!tabOptions.isEmpty()) {
            return new ArrayList<>(tabOptions);
        }
        return null;
    }
}
