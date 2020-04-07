package com.github.intellectualsites.plotsquared.bukkit.placeholders;

import com.github.intellectualsites.plotsquared.bukkit.BukkitMain;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {

    public Placeholders(BukkitMain plugin) {
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
        return "2.3";
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        final PlotPlayer pl = PlotPlayer.get(p.getName());
        final Plot plot = pl.getCurrentPlot();
        if (pl == null) {
            return "";
        }

        if (identifier.startsWith("has_plot_")) {
            if (identifier.split("has_plot_").length != 2) return null;

            identifier = identifier.split("has_plot_")[1];
            return pl.getPlotCount(identifier) > 0 ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        if (identifier.startsWith("plot_count_")) {
            if (identifier.split("plot_count_").length != 2) return null;

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
                final String name = UUIDHandler.getName(uid);
                return (name != null) ? name : ((Bukkit.getOfflinePlayer(uid) != null) ? Bukkit.getOfflinePlayer(uid).getName() : "unknown");
            }
            case "currentplot_world": {
                return p.getWorld().getName();
            }
            case "has_plot": {
                return (pl.getPlotCount() > 0) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
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
                if (pl.getCurrentPlot().getMembers() == null && pl.getCurrentPlot().getTrusted() == null) {
                    return "0";
                }
                return String.valueOf(pl.getCurrentPlot().getMembers().size() + pl.getCurrentPlot().getTrusted().size());
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
                return (pl.getCurrentPlot() != null) ? ((pl.getCurrentPlot().isAdded(pl.getUUID())) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse()) : "";
            }
            case "currentplot_x": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return String.valueOf(plot.getId().x);
            }
            case "currentplot_y": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return String.valueOf(plot.getId().y);
            }
            case "currentplot_xy": {
                if (pl.getCurrentPlot() == null) {
                    return "";
                }
                return pl.getCurrentPlot().getId().x + ";" + pl.getCurrentPlot().getId().y;
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
        return null;
    }
}
