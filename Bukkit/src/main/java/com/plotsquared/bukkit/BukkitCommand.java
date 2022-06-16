/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.Settings;
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
import java.util.Locale;

public class BukkitCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(
            CommandSender commandSender, Command command, String commandLabel,
            String[] args
    ) {
        if (commandSender instanceof Player) {
            return MainCommand.onCommand(BukkitUtil.adapt((Player) commandSender), args);
        }
        if (commandSender instanceof ConsoleCommandSender
                || commandSender instanceof ProxiedCommandSender
                || commandSender instanceof RemoteConsoleCommandSender) {
            return MainCommand.onCommand(ConsolePlayer.getConsole(), args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender commandSender, Command command, String label,
            String[] args
    ) {
        if (!(commandSender instanceof Player)) {
            return null;
        }
        PlotPlayer<?> player = BukkitUtil.adapt((Player) commandSender);
        if (args.length == 0) {
            return Collections.singletonList("plots");
        }
        if (!Settings.Enabled_Components.TAB_COMPLETED_ALIASES.contains(label.toLowerCase(Locale.ENGLISH))) {
            return List.of();
        }
        Collection<com.plotsquared.core.command.Command> objects =
                MainCommand.getInstance().tab(player, args, label.endsWith(" "));
        if (objects == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (com.plotsquared.core.command.Command o : objects) {
            result.add(o.toString());
        }
        return result;
    }

}
