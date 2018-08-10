package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.bukkit.commands.DebugUUID;
import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    public BukkitCommand() {
        new DebugUUID();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel,
        String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.getPlayer((Player) commandSender), args);
        }
        if (commandSender instanceof ConsoleCommandSender
            || commandSender instanceof ProxiedCommandSender
            || commandSender instanceof RemoteConsoleCommandSender) {
            return MainCommand.onCommand(ConsolePlayer.getConsole(), args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s,
        String[] args) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        PlotPlayer player = BukkitUtil.getPlayer((Player) commandSender);
        if (args.length == 0) {
            return Collections.singletonList("plots");
        }
        Collection objects = MainCommand.getInstance().tab(player, args, s.endsWith(" "));
        if (objects == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (Object o : objects) {
            result.add(o.toString());
        }
        return result.isEmpty() ? null : result;
    }
}
