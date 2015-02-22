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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Rate extends SubCommand {
    /*
     * String cmd, String permission, String description, String usage, String
     * alias, CommandCategory category
     */
    public Rate() {
        super("rate", "plots.rate", "Rate the plot", "rate {0-10}", "rt", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            sendMessage(plr, C.RATING_NOT_VALID);
            return true;
        }
        Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(plr, C.RATING_NOT_OWNED);
            return true;
        }
        if (plot.getOwner().equals(UUIDHandler.getUUID(plr))) {
            sendMessage(plr, C.RATING_NOT_YOUR_OWN);
            return true;
        }
        final String arg = args[0];
        boolean o = false;
        for (final char c : arg.toCharArray()) {
            if (!Character.isDigit(c)) {
                o = true;
                break;
            }
        }
        int rating = 0;
        if (!o) {
            rating = Integer.parseInt(arg);
        }
        if (o || ((rating < 0) || (rating > 10))) {
            sendMessage(plr, C.RATING_NOT_VALID);
            return true;
        }
        // TODO implement check for already rated
        boolean rated = true;
        try {
            DBFunc.getRatings(plot);
        } catch (final Exception e) {
            rated = false;
        }
        if (rated) {
            sendMessage(plr, C.RATING_ALREADY_EXISTS, plot.getId().toString());
        }
        final boolean success = true;
        if (success) {
            sendMessage(plr, C.RATING_APPLIED, plot.getId().toString());
        } else {
            sendMessage(plr, C.COMMAND_WENT_WRONG);
        }
        return true;
    }
}
