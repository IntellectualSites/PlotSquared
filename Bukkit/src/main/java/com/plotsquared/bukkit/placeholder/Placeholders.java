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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.MainUtil;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {

    public Placeholders() {
    }

    @Override public boolean persist() {
        return true;
    }

    @Override public boolean canRegister() {
        return true;
    }

    @Override public String getAuthor() {
        return "NotMyFault";
    }

    @Override public String getIdentifier() {
        return "plotsquared";
    }

    @Override public String getVersion() {
        return "2.5";
    }

    @Override public String onPlaceholderRequest(Player p, String identifier) {
        final PlotPlayer pl = PlotSquared.imp().getPlayerManager().getPlayerIfExists(p.getUniqueId());

        if (pl == null) {
            return "";
        }

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

        switch (identifier) {
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
        }

        Plot plot = pl.getCurrentPlot();

        if (plot == null) {
            return "";
        }

        switch (identifier) {
            case "currentplot_alias": {
                return plot.getAlias();
            }
            case "currentplot_owner": {
                final UUID plotOwner = plot.getOwnerAbs();
                if (plotOwner == null) {
                    return "";
                }

                try {
                    return MainUtil.getName(plotOwner, false);
                } catch (final Exception ignored) {}

                final String name = Bukkit.getOfflinePlayer(plotOwner).getName();
                return name != null ? name : "unknown";
            }
            case "currentplot_members": {
                if (plot.getMembers() == null && plot.getTrusted() == null) {
                    return "0";
                }
                return String.valueOf(plot.getMembers().size() + plot.getTrusted().size());
            }
            case "currentplot_members_added": {
                if (plot.getMembers() == null) {
                    return "0";
                }
                return String.valueOf(plot.getMembers().size());
            }
            case "currentplot_members_trusted": {
                if (plot.getTrusted() == null) {
                    return "0";
                }
                return String.valueOf(plot.getTrusted().size());
            }
            case "currentplot_members_denied": {
                if (plot.getDenied() == null) {
                    return "0";
                }
                return String.valueOf(plot.getDenied().size());
            }
            case "has_build_rights": {
                return plot.isAdded(pl.getUUID()) ?
                    PlaceholderAPIPlugin.booleanTrue() :
                    PlaceholderAPIPlugin.booleanFalse();
            }
            case "currentplot_x": {
                return String.valueOf(plot.getId().getX());
            }
            case "currentplot_y": {
                return String.valueOf(plot.getId().getY());
            }
            case "currentplot_xy": {
                return plot.getId().getX() + ";" + plot.getId().getY();
            }
            case "currentplot_rating": {
                return String.valueOf(plot.getAverageRating());
            }
            case "currentplot_biome": {
                return plot.getBiomeSynchronous() + "";
            }
            default:
                break;
        }
        if (identifier.startsWith("currentplot_localflag_")) {
            return getFlagValue(plot, identifier.substring("currentplot_localflag_".length()),
                false);
        }
        if (identifier.startsWith("currentplot_flag_")) {
            return getFlagValue(plot, identifier.substring("currentplot_flag_".length()), true);
        }
        return "";
    }

    /**
     * Return the flag value from its name on the current plot.
     * If the flag doesn't exist it returns an empty string.
     * If the flag exists but it is not set on current plot and the parameter inherit is set to true,
     * it returns the default value.
     *
     * @param plot     Current plot where the player is
     * @param flagName Name of flag to get from current plot
     * @param inherit  Define if it returns only the flag set on currentplot or also inherited flag
     * @return The value of flag serialized in string
     */
    private String getFlagValue(final Plot plot, final String flagName, final boolean inherit) {
        if (flagName.isEmpty())
            return "";
        final PlotFlag<?, ?> flag = GlobalFlagContainer.getInstance().getFlagFromString(flagName);
        if (flag == null)
            return "";

        if (inherit) {
            return plot.getFlag(flag).toString();
        } else {
            final PlotFlag<?, ?> plotFlag = plot.getFlagContainer().queryLocal(flag.getClass());
            return (plotFlag != null) ? plotFlag.getValue().toString() : "";
        }
    }
}
