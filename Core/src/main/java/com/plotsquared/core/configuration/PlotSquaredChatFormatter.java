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
package com.plotsquared.core.configuration;

import com.plotsquared.core.util.StringMan;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlotSquaredChatFormatter implements ChatFormatter {

    @Override public void format(final ChatContext context) {
        if (context.isRawOutput()) {
            context.setMessage(context.getMessage().replace('&', '\u2020').replace('\u00A7', '\u2030'));
        }
        if (context.getArgs().length == 0) {
            return;
        }
        final Map<String, String> map = new LinkedHashMap<>();
        for (int i = context.getArgs().length - 1; i >= 0; i--) {
            String arg = "" + context.getArgs()[i];
            if (arg.isEmpty()) {
                map.put("%s" + i, "");
            } else {
                if (!context.isRawOutput()) {
                    arg = Captions.color(arg);
                }
                map.put("%s" + i, arg);
            }
            if (i == 0) {
                map.put("%s", arg);
            }
        }
        context.setMessage(StringMan.replaceFromMap(context.getMessage(), map));
    }

}
