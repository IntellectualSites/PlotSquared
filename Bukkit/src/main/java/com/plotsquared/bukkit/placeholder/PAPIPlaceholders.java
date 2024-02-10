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
package com.plotsquared.bukkit.placeholder;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.query.PlotQuery;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PAPIPlaceholders extends PlaceholderExpansion {

    public PAPIPlaceholders() {
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "IntellectualSites";
    }

    @Override
    public String getIdentifier() {
        return "plotsquared";
    }

    @Override
    public String getVersion() {
        return "3";
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        final PlotPlayer<?> pl = PlotSquared.platform().playerManager().getPlayerIfExists(p.getUniqueId());

        if (pl == null) {
            return "";
        }

        // PAPI specific ones that don't translate well over into other placeholder APIs
        if (identifier.startsWith("has_plot_")) {
            identifier = identifier.substring("has_plot_".length());
            if (identifier.isEmpty()) {
                return "";
            }

            return pl.getPlotCount(identifier) > 0 ?
                    PlaceholderAPIPlugin.booleanTrue() :
                    PlaceholderAPIPlugin.booleanFalse();
        }

        if (identifier.startsWith("plot_count_")) {
            identifier = identifier.substring("plot_count_".length());
            if (identifier.isEmpty()) {
                return "";
            }

            return String.valueOf(pl.getPlotCount(identifier));
        }

        if (identifier.startsWith("base_plot_count_")) {
            identifier = identifier.substring("base_plot_count_".length());
            if (identifier.isEmpty()) {
                return "";
            }

            return String.valueOf(PlotQuery.newQuery()
                    .ownedBy(pl)
                    .inWorld(identifier)
                    .whereBasePlot()
                    .thatPasses(plot -> !DoneFlag.isDone(plot))
                    .count());
        }

        // PlotSquared placeholders
        return PlotSquared.platform().placeholderRegistry().getPlaceholderValue(identifier, pl);
    }

}
