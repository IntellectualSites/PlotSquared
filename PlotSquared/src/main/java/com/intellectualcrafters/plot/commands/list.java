////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.UUIDHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Citymonstret
 */
public class list extends SubCommand {

    public list() {
        super(Command.LIST, "List all plots", "list {mine|shared|all|world}", CommandCategory.INFO, false);
    }

    private static String getName(final UUID id) {
        if (id == null) {
            return "none";
        }
        /*
         * String name = Bukkit.getOfflinePlayer(id).getName(); if (name ==
         * null) { return "none"; } return name;
         */
        String name = UUIDHandler.getName(id);
        if (name == null) {
            return "unknown";
        }
        return name;
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length < 1) {
            final StringBuilder builder = new StringBuilder();
            builder.append(C.SUBCOMMAND_SET_OPTIONS_HEADER.s());
            if (plr != null) {
                builder.append(getArgumentList(new String[]{"mine", "shared", "world", "all"}));
            } else {
                builder.append(getArgumentList(new String[]{"all"}));
            }
            PlayerFunctions.sendMessage(plr, builder.toString());
            return true;
        }
        if (args[0].equalsIgnoreCase("mine") && (plr != null)) {
            final StringBuilder string = new StringBuilder();
            string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "your")).append("\n");
            int idx = 0;
            for (final Plot p : PlotMain.getPlots(plr)) {
                string.append(C.PLOT_LIST_ITEM_ORDERED.s().replaceAll("%in", idx + 1 + "").replaceAll("%id", p.id.toString()).replaceAll("%world", p.world).replaceAll("%owner", getName(p.owner))).append("\n");
                idx++;
            }
            if (idx == 0) {
                PlayerFunctions.sendMessage(plr, C.NO_PLOTS);
                return true;
            }
            string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "You have").replaceAll("%num%", idx + "").replaceAll("%plot%", idx == 1 ? "plot" : "plots"));
            PlayerFunctions.sendMessage(plr, string.toString());
            return true;
        } else if (args[0].equalsIgnoreCase("shared") && (plr != null)) {
            final StringBuilder string = new StringBuilder();
            string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "all")).append("\n");
            for (final Plot p : PlotMain.getPlotsSorted()) {
                if (p.helpers.contains(plr.getUniqueId())) {
                    string.append(C.PLOT_LIST_ITEM.s().replaceAll("%id", p.id.toString()).replaceAll("%world", p.world).replaceAll("%owner", getName(p.owner))).append("\n");
                }
            }
            string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There are").replaceAll("%num%", PlotMain.getPlotsSorted().size() + "").replaceAll("%plot%", PlotMain.getPlotsSorted().size() == 1 ? "plot" : "plots"));
            PlayerFunctions.sendMessage(plr, string.toString());
            return true;
        } else if (args[0].equalsIgnoreCase("all")) {
            // Current page
            int page = 0;

            // is a page specified? else use 0
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                    --page;
                    if (page < 0) {
                        page = 0;
                    }
                } catch (final Exception e) {
                    page = 0;
                }
            }

            // Get the total pages
            // int totalPages = ((int) Math.ceil(12 *
            // (PlotMain.getPlotsSorted().size()) / 100));
            final int totalPages = (int) Math.ceil(PlotMain.getPlotsSorted().size() / 12);

            if (page > totalPages) {
                page = totalPages;
            }

            // Only display 12!
            int max = (page * 12) + 12;

            if (max > PlotMain.getPlotsSorted().size()) {
                max = PlotMain.getPlotsSorted().size();
            }

            final StringBuilder string = new StringBuilder();

            string.append(C.PLOT_LIST_HEADER_PAGED.s().replaceAll("%cur", page + 1 + "").replaceAll("%max", totalPages + 1 + "").replaceAll("%word%", "all")).append("\n");
            Plot p;

            // This might work xD
            for (int x = (page * 12); x < max; x++) {
                p = (Plot) PlotMain.getPlotsSorted().toArray()[x];
                string.append(C.PLOT_LIST_ITEM_ORDERED.s().replaceAll("%in", x + 1 + "").replaceAll("%id", p.id.toString()).replaceAll("%world", p.world).replaceAll("%owner", getName(p.owner))).append("\n");
            }

            string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There is").replaceAll("%num%", PlotMain.getPlotsSorted().size() + "").replaceAll("%plot%", PlotMain.getPlotsSorted().size() == 1 ? "plot" : "plots"));
            PlayerFunctions.sendMessage(plr, string.toString());
            return true;
        } else if (args[0].equalsIgnoreCase("world") && (plr != null)) {
            final StringBuilder string = new StringBuilder();
            string.append(C.PLOT_LIST_HEADER.s().replaceAll("%word%", "all")).append("\n");
            final HashMap<PlotId, Plot> plots = PlotMain.getPlots(plr.getWorld());
            for (final Plot p : plots.values()) {
                string.append(C.PLOT_LIST_ITEM.s().replaceAll("%id", p.id.toString()).replaceAll("%world", p.world).replaceAll("%owner", getName(p.owner))).append("\n");
            }
            string.append(C.PLOT_LIST_FOOTER.s().replaceAll("%word%", "There is").replaceAll("%num%", plots.values().size() + "").replaceAll("%plot%", plots.values().size() == 1 ? "plot" : "plots"));
            PlayerFunctions.sendMessage(plr, string.toString());
            return true;
        } else {
            // execute(plr);
            sendMessage(plr, C.DID_YOU_MEAN, new StringComparison(args[0], new String[]{"mine", "shared", "world", "all"}).getBestMatch());
            return false;
        }
    }

    private String getArgumentList(final String[] strings) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : strings) {
            builder.append(getString(s));
        }
        return builder.toString().substring(1, builder.toString().length() - 1);
    }

    private String getString(final String s) {
        return ChatColor.translateAlternateColorCodes('&', C.BLOCK_LIST_ITEM.s().replaceAll("%mat%", s));
    }

}
