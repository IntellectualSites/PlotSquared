/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.bukkit.commands.DebugUUID;
import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
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
        Collection<com.github.intellectualsites.plotsquared.commands.Command> objects = MainCommand.getInstance().tab(player, args, s.endsWith(" "));
        if (objects == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (com.github.intellectualsites.plotsquared.commands.Command o : objects) {
            result.add(o.toString());
        }
        return result.isEmpty() ? null : result;
    }
}
