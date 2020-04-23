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
package com.plotsquared.bukkit.placeholder;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.uuid.UUIDHandler;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {

    public Placeholders() {
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
        return "NotMyFault";
    }

    @Override
    public String getIdentifier() {
        return "plotsquared";
    }

    @Override
    public String getVersion() {
        return "2.4";
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        final PlotPlayer pl = PlotPlayer.get(p.getName());
        final Plot plot = pl.getCurrentPlot();

        if (identifier.startsWith("has_plot_")) {
            if (identifier.split("has_plot_").length != 2)
                return "";

            identifier = identifier.split("has_plot_")[1];
            return pl.getPlotCount(identifier) > 0 ?
                PlaceholderAPIPlugin.booleanTrue() :
                PlaceholderAPIPlugin.booleanFalse();
        }

        if (identifier.startsWith("plot_count_")) {
            if (identifier.split("plot_count_").length != 2)
                return "";

            identifier = identifier.split("plot_count_")[1];
            return String.valueOf(pl.getPlotCount(identifier));
        }

        switch (identifier) {
            case "currentplot_alias": {
                return (pl.getCurrentPlot() != null) ? pl.getCurrentPlot().getAlias() : "";
            }
            case "currentplot_owner": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                final Set<UUID> o = pl.getCurrentPlot().getOwners();
                if (o == null || o.isEmpty()) {
                    return "";
                }
                final UUID uid = (UUID) o.toArray()[0];
                if (uid == null) {
                    return "";
                }
                String name = UUIDHandler.getName(uid);

                if (name != null) {
                    return name;
                }

                name = Bukkit.getOfflinePlayer(uid).getName();
                return name != null ? name : "unknown";
            }
            case "currentplot_world": {
                return p.getWorld().getName();
            }
            case "has_plot": {
                return (pl.getPlotCount() > 0) ?
                    PlaceholderAPIPlugin.booleanTrue() :
                    PlaceholderAPIPlugin.booleanFalse();
            }
            case "allowed_plot_count": {
                return String.valueOf(pl.getAllowedPlots());
            }
            case "plot_count": {
                return String.valueOf(pl.getPlotCount());
            }
            case "currentplot_members": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                if (pl.getCurrentPlot().getMembers() == null
                    && pl.getCurrentPlot().getTrusted() == null) {
                    return "0";
                }
                return String.valueOf(
                    pl.getCurrentPlot().getMembers().size() + pl.getCurrentPlot().getTrusted()
                        .size());
            }
            case "currentplot_members_added": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                if (pl.getCurrentPlot().getMembers() == null) {
                    return "0";
                }
                return String.valueOf(pl.getCurrentPlot().getMembers().size());
            }
            case "currentplot_members_trusted": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                if (pl.getCurrentPlot().getTrusted() == null) {
                    return "0";
                }
                return String.valueOf(plot.getTrusted().size());
            }
            case "currentplot_members_denied": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                if (pl.getCurrentPlot().getDenied() == null) {
                    return "0";
                }
                return String.valueOf(pl.getCurrentPlot().getDenied().size());
            }
            case "has_build_rights": {
                return (pl.getCurrentPlot() != null) ?
                    ((pl.getCurrentPlot().isAdded(pl.getUUID())) ?
                        PlaceholderAPIPlugin.booleanTrue() :
                        PlaceholderAPIPlugin.booleanFalse()) :
                    "";
            }
            case "currentplot_x": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return String.valueOf(plot.getId().getX());
            }
            case "currentplot_y": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return String.valueOf(plot.getId().getY());
            }
            case "currentplot_xy": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return pl.getCurrentPlot().getId().getX() + ";" + pl.getCurrentPlot().getId().getY();
            }
            case "currentplot_rating": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return String.valueOf(plot.getAverageRating());
            }
            case "currentplot_biome": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return plot.getBiomeSynchronous() + "";
            }
            default:
                break;
        }
        return "";
    }
}
