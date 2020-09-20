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

package com.plotsquared.bukkit.placeholder;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PAPIPlaceholders extends PlaceholderExpansion {

    public PAPIPlaceholders() {
    }

    @Override public boolean persist() {
        return true;
    }

    @Override public boolean canRegister() {
        return true;
    }

    @Override public String getAuthor() {
        return "IntellectualSites";
    }

    @Override public String getIdentifier() {
        return "plotsquared";
    }

    @Override public String getVersion() {
        return "3";
    }

    @Override public String onPlaceholderRequest(Player p, String identifier) {
        final PlotPlayer<?> pl = PlotSquared.platform().getPlayerManager().getPlayerIfExists(p.getUniqueId());

        if (pl == null) {
            return "";
        }

        // PAPI specific ones that don't translate well over into other placeholder APIs
        if (identifier.startsWith("has_plot_")) {
            identifier = identifier.substring("has_plot_".length());
            if (identifier.isEmpty())
                return "";

            return pl.getPlotCount(identifier) > 0 ?
                PlaceholderAPIPlugin.booleanTrue() :
                PlaceholderAPIPlugin.booleanFalse();
        }

        if (identifier.startsWith("plot_count_")) {
            identifier = identifier.substring("plot_count_".length());
            if (identifier.isEmpty())
                return "";

            return String.valueOf(pl.getPlotCount(identifier));
        }

        // PlotSquared placeholders
        return PlotSquared.get().getPlaceholderRegistry().getPlaceholderValue(identifier, pl);
    }

}
