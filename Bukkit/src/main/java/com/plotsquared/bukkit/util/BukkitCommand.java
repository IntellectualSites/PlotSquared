package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.commands.DebugUUID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    public BukkitCommand() {
        new DebugUUID();
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, org.bukkit.command.Command command, final String commandLabel,
            String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.getPlayer((Player) commandSender), args);
        }
        if (commandSender == null || commandSender.getClass() == Bukkit.getConsoleSender().getClass()) {
            return MainCommand.onCommand(ConsolePlayer.getConsole(), args);
        }
        @SuppressWarnings("deprecation")
        ConsolePlayer sender = new ConsolePlayer() {
            @Override
            public void sendMessage(String message) {
                commandSender.sendMessage(commandLabel);
            }

            @Override
            public boolean hasPermission(String permission) {
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
        boolean result = MainCommand.onCommand(sender, args);
        ConsolePlayer.getConsole().teleport(sender.getLocationFull());
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] args) {
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
        return result;
    }
}
