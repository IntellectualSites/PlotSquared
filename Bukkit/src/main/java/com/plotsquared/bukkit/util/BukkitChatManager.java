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
package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.chat.FancyMessage;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.message.PlotMessage;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BukkitChatManager extends ChatManager<FancyMessage> {

    @Override public FancyMessage builder() {
        return new FancyMessage("");
    }

    @Override public void color(PlotMessage message, String color) {
        message.$(this).color(ChatColor.getByChar(Captions.color(color).substring(1)));
    }

    @Override public void tooltip(PlotMessage message, PlotMessage... tooltips) {
        List<FancyMessage> lines =
            Arrays.stream(tooltips).map(tooltip -> tooltip.$(this)).collect(Collectors.toList());
        message.$(this).formattedTooltip(lines);
    }

    @Override public void command(PlotMessage message, String command) {
        message.$(this).command(command);
    }

    @Override public void text(PlotMessage message, String text) {
        message.$(this).then(ChatColor.stripColor(text));
    }

    @Override public void send(PlotMessage plotMessage, PlotPlayer player) {
        if (player instanceof ConsolePlayer || !Settings.Chat.INTERACTIVE) {
            player.sendMessage(plotMessage.$(this).toOldMessageFormat());
        } else {
            plotMessage.$(this).send(((BukkitPlayer) player).player);
        }
    }

    @Override public void suggest(PlotMessage plotMessage, String command) {
        plotMessage.$(this).suggest(command);
    }

}
