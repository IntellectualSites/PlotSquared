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
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;

/**
 * Created 2014-08-01 for PlotSquared
 *
 * @author Empire92
 */
public class Copy extends SubCommand {
    public Copy() {
        super("copy", "plots.copy", "Copy a plot", "copypaste", "", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.NEED_PLOT_ID);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot copy <X;Z>");
            return false;
        }
        final Location loc = plr.getLocation();
        final Plot plot1 = MainUtil.getPlot(loc);
        if (plot1 == null) {
            return !MainUtil.sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot1.isAdded(plr.getUUID()) && !plr.hasPermission(Permissions.ADMIN.s))  {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        final String world = loc.getWorld();
        final PlotId plot2 = MainUtil.parseId(args[0]);
        if ((plot2 == null)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot copy <X;Z>");
            return false;
        }
        if (plot1.id.equals(plot2)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot copy <X;Z>");
            return false;
        }
        if (MainUtil.copy(world, plot1.id, plot2, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, C.COPY_SUCCESS);
            }
        })) {
            return true;
        } else {
            MainUtil.sendMessage(plr, C.REQUIRES_UNOWNED);
            return false;
        }
    }
}
