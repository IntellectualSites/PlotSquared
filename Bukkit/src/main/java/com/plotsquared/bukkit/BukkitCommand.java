package com.plotsquared.bukkit;

import com.plotsquared.bukkit.command.DebugUUID;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.TabCompleter;
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
        Collection<com.plotsquared.core.command.Command> objects = MainCommand.getInstance().tab(player, args, s.endsWith(" "));
        if (objects == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (com.plotsquared.core.command.Command o : objects) {
            result.add(o.toString());
        }
        return result.isEmpty() ? null : result;
    }
}
