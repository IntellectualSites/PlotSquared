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
package com.plotsquared.bukkit.commands;

import cloud.commandframework.SenderMapper;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Mapper between {@link CommandSender} and {@link PlotPlayer}.
 */
public final class BukkitSenderMapper implements SenderMapper<CommandSender, PlotPlayer<?>> {

    @Override
    public @NonNull PlotPlayer<?> map(final @NonNull CommandSender base) {
        if (base instanceof Player player) {
            return BukkitUtil.adapt(player);
        }
        return ConsolePlayer.getConsole();
    }

    @Override
    public @NonNull CommandSender reverse(final @NonNull PlotPlayer<?> mapped) {
        if (mapped instanceof ConsolePlayer) {
            return Bukkit.getConsoleSender();
        }
        return (Player) mapped.getPlatformPlayer();
    }
}
