package com.github.intellectualsites.plotsquared.nukkit.util;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.RemoteConsoleCommandSender;
import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;

public class NukkitCommand extends Command {

    public NukkitCommand(String cmd, String[] aliases) {
        super(cmd, "Plot command", "/plot", aliases);
    }


    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(NukkitUtil.getPlayer((Player) commandSender), args);
        }
        if (commandSender instanceof ConsoleCommandSender
            || commandSender instanceof RemoteConsoleCommandSender) {
            return MainCommand.onCommand(ConsolePlayer.getConsole(), args);
        }
        return false;
    }
}
