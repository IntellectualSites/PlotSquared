/*
 *
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
package com.plotsquared.core.plot.message;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.ChatManager;

import java.util.ArrayList;
import java.util.List;

public class PlainChatManager extends ChatManager<List<StringBuilder>> {

    @Override public List<StringBuilder> builder() {
        return new ArrayList<>();
    }

    @Override public void color(PlotMessage message, String color) {
        List<StringBuilder> parts = message.$(this);
        parts.get(parts.size() - 1).insert(0, color);
    }

    @Override public void tooltip(PlotMessage message, PlotMessage... tooltips) {
    }

    @Override public void command(PlotMessage message, String command) {
    }

    @Override public void text(PlotMessage message, String text) {
        message.$(this).add(new StringBuilder(Captions.color(text)));
    }

    @Override public void send(PlotMessage plotMessage, PlotPlayer player) {
        StringBuilder built = new StringBuilder();
        for (StringBuilder sb : plotMessage.$(this)) {
            built.append(sb);
        }
        player.sendMessage(built.toString());
    }

    @Override public void suggest(PlotMessage plotMessage, String command) {
    }

}
