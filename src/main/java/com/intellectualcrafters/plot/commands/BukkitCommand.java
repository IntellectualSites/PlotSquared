package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public class BukkitCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String commandLabel, final String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.getPlayer((Player) commandSender), commandLabel, args);
        }
        return MainCommand.onCommand(null, commandLabel, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        final PlotPlayer player = BukkitUtil.getPlayer((Player) commandSender);
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
        final String[] commands = new String[MainCommand.subCommands.size()];
        for (int x = 0; x < MainCommand.subCommands.size(); x++) {
            commands[x] = MainCommand.subCommands.get(x).cmd;
        }
        String best = new StringComparison(strings[0], commands).getBestMatch();
        tabOptions.add(best);
        final String arg = strings[0].toLowerCase();
        for (final SubCommand cmd : MainCommand.subCommands) {
            if (!cmd.cmd.equalsIgnoreCase(best)) {
                if (cmd.permission.hasPermission(player)) {
                    if (cmd.cmd.startsWith(arg)) {
                        tabOptions.add(cmd.cmd);
                    } else if (cmd.alias.size() > 0 && cmd.alias.get(0).startsWith(arg)) {
                        tabOptions.add(cmd.alias.get(0));
                    }
                }
            }
        }
        if (tabOptions.size() > 0) {
            return tabOptions;
        }
        return null;
    }
}
