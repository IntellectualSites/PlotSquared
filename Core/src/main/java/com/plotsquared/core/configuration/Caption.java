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

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.StringMan;

public interface Caption {

    String getTranslated();

    default String formatted() {
        return StringMan.replaceFromMap(getTranslated(), Captions.replacements);
    }

    default boolean send(PlotPlayer caller, String... args) {
        return send(caller, (Object[]) args);
    }

    default boolean send(PlotPlayer caller, Object... args) {
        String msg = CaptionUtility.format(caller, this, args);
        if (caller == null) {
            // TODO: Replace with adventure
            System.out.println(msg);
        } else {
            caller.sendMessage(msg);
        }
        return true;
    }

    boolean usePrefix();

}
