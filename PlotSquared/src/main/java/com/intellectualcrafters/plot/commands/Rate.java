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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;

public class Rate extends SubCommand {
    /*
     * String cmd, String permission, String description, String usage, String
     * alias, CommandCategory category
     */
    public Rate() {
        super("rate", "plots.rate", "Rate the plot", "rate [#|next]", "rt", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            sendMessage(plr, C.RATING_NOT_VALID);
            return true;
        }
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(plr, C.RATING_NOT_OWNED);
            return true;
        }
        if (plot.isOwner(plr.getUUID())) {
            sendMessage(plr, C.RATING_NOT_YOUR_OWN);
            return true;
        }
        final String arg = args[0];
        
        if (arg.equalsIgnoreCase("next")) {
            ArrayList<Plot> plots = new ArrayList<>(PlotSquared.getPlots());
            Collections.sort(plots, new Comparator<Plot>() {
                @Override
                public int compare(Plot p1, Plot p2) {
                    int v1 = 0;
                    int v2 = 0;
                    if (p1.settings.ratings != null) {
                        for (Entry<UUID, Integer> entry : p1.settings.ratings.entrySet()) {
                            v1 -= 11 - entry.getValue();
                        }
                    }
                    if (p2.settings.ratings != null) {
                        for (Entry<UUID, Integer> entry : p2.settings.ratings.entrySet()) {
                            v2 -= 11 - entry.getValue();
                        }
                    }
                    return v2 - v1;
                }
            });
            UUID uuid = plr.getUUID();
            for (Plot p : plots) {
                if (plot.settings.ratings == null || !plot.settings.ratings.containsKey(uuid)) {
                    MainUtil.teleportPlayer(plr, plr.getLocation(), p);
                    MainUtil.sendMessage(plr, C.RATE_THIS);
                    return true;
                }
            }
            MainUtil.sendMessage(plr, C.FOUND_NO_PLOTS);
            return false;
        }
        
        final int rating;
        if (StringUtils.isNumeric(arg) && arg.length() < 3 && arg.length() > 0) {
            rating = Integer.parseInt(arg);
            if (rating > 10) {
                sendMessage(plr, C.RATING_NOT_VALID);
                return false;
            }
        }
        else {
            sendMessage(plr, C.RATING_NOT_VALID);
            return false;
        }
        final UUID uuid = plr.getUUID();
        final Runnable run = new Runnable() {
            @Override
            public void run() {
                if (plot.settings.ratings.containsKey(uuid)) {
                    sendMessage(plr, C.RATING_ALREADY_EXISTS, plot.getId().toString());
                    return;
                }
                plot.settings.ratings.put(uuid, rating);
                DBFunc.setRating(plot, uuid, rating);
                sendMessage(plr, C.RATING_APPLIED, plot.getId().toString());
            }
        };
        if (plot.settings.ratings == null) {
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    plot.settings.ratings = DBFunc.getRatings(plot);
                    run.run();
                }
            });
            return true;
        }
        run.run();
        return true;
    }
}
