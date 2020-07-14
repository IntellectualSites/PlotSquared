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
package com.plotsquared.core.plot.message;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.ChatManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotMessage {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + PlotMessage.class.getSimpleName());

    private Object builder;

    public PlotMessage() {
        try {
            reset(ChatManager.manager);
        } catch (Throwable e) {
            logger.error("[P2] {} doesn't support fancy chat for {}", PlotSquared.platform().getPluginName(),
                PlotSquared.platform().getServerVersion());
            ChatManager.manager = new PlainChatManager();
            reset(ChatManager.manager);
        }
    }

    public PlotMessage(String text) {
        this();
        text(text);
    }

    public <T> T $(ChatManager<T> manager) {
        return (T) this.builder;
    }

    public <T> T reset(ChatManager<T> manager) {
        return (T) (this.builder = manager.builder());
    }

    public PlotMessage text(String text) {
        ChatManager.manager.text(this, text);
        return this;
    }

    public PlotMessage tooltip(PlotMessage... tooltip) {
        ChatManager.manager.tooltip(this, tooltip);
        return this;
    }

    public PlotMessage tooltip(String tooltip) {
        return tooltip(new PlotMessage(tooltip));
    }

    public PlotMessage command(String command) {
        ChatManager.manager.command(this, command);
        return this;
    }

    public PlotMessage suggest(String command) {
        ChatManager.manager.suggest(this, command);
        return this;
    }

    public PlotMessage color(String color) {
        ChatManager.manager.color(this, Captions.color(color));
        return this;
    }

    public void send(PlotPlayer player) {
        ChatManager.manager.send(this, player);
    }

}
