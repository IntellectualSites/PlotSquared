package com.plotsquared.bukkit.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualsites.commands.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.StringComparison;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public class BukkitCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(final CommandSender commandSender, final org.bukkit.command.Command command, final String commandLabel, final String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.getPlayer((Player) commandSender), commandLabel, args);
        }
        return MainCommand.onCommand(null, commandLabel, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final org.bukkit.command.Command command, final String s, final String[] strings) {
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
        if (!command.getLabel().equalsIgnoreCase("plots")) {
            return null;
        }
        final List<String> tabOptions = new ArrayList<>();
        final String[] commands = new String[MainCommand.instance.getCommands().size()];
        for (int x = 0; x < MainCommand.instance.getCommands().size(); x++) {
            commands[x] = MainCommand.instance.getCommands().get(x).getCommand();
        }
        String best = new StringComparison(strings[0], commands).getBestMatch();
        tabOptions.add(best);
        final String arg = strings[0].toLowerCase();
        for (final Command cmd : MainCommand.instance.getCommands()) {
            if (!cmd.getCommand().equalsIgnoreCase(best)) {
                if (player.hasPermission(cmd.getPermission())) {
                    if (cmd.getCommand().startsWith(arg)) {
                        tabOptions.add(cmd.getCommand());
                    } else if (cmd.getAliases().length > 0 && cmd.getAliases()[0].startsWith(arg)) {
                        tabOptions.add(cmd.getAliases()[0]);
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
