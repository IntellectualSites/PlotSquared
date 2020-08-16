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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.google.inject.Inject;
import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.placeholder.MVdWPlaceholders;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import javax.annotation.Nonnull;

public class ServerListener implements Listener {

    private final BukkitPlatform plugin;

    @Inject public ServerListener(@Nonnull final BukkitPlatform plugin) {
        this.plugin = plugin;
    }

    @EventHandler public void onServerLoad(ServerLoadEvent event) {
        if (Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI") != null) {
            new MVdWPlaceholders(this.plugin, PlotSquared.get().getPlaceholderRegistry());
            ConsolePlayer.getConsole().sendMessage(TranslatableCaption.of("placeholder.hooked"));
        }
    }
}
