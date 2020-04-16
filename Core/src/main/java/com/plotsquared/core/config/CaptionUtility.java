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
package com.plotsquared.core.config;

import com.plotsquared.core.player.PlotPlayer;

public class CaptionUtility {

    public static String formatRaw(PlotPlayer recipient, String message, Object... args) {
        final ChatFormatter.ChatContext chatContext = new ChatFormatter.ChatContext(recipient, message, args, true);
        for (final ChatFormatter chatFormatter : ChatFormatter.formatters) {
            chatFormatter.format(chatContext);
        }
        return chatContext.getMessage();
    }

    public static String format(PlotPlayer recipient, String message, Object... args) {
        final ChatFormatter.ChatContext chatContext = new ChatFormatter.ChatContext(recipient, message, args, false);
        for (final ChatFormatter chatFormatter : ChatFormatter.formatters) {
            chatFormatter.format(chatContext);
        }
        return chatContext.getMessage();
    }

    public static String format(PlotPlayer recipient, Caption caption, Object... args) {
        if (caption.usePrefix() && caption.getTranslated().length() > 0) {
            return Captions.PREFIX.getTranslated() + format(recipient, caption.getTranslated(), args);
        } else {
            return format(recipient, caption.getTranslated(), args);
        }
    }

}
