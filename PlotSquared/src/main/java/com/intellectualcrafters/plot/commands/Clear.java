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

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Clear extends SubCommand {
    public Clear() {
        super(Command.CLEAR, "Clear a plot", "clear", CommandCategory.ACTIONS, false);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr == null) {
            // Is console
            if (args.length < 2) {
                PlotSquared.log("You need to specify two arguments: ID (0;0) & World (world)");
            } else {
                final PlotId id = PlotId.fromString(args[0]);
                final String world = args[1];
                if (id == null) {
                    PlotSquared.log("Invalid Plot ID: " + args[0]);
                } else {
                    if (!PlotSquared.isPlotWorld(world)) {
                        PlotSquared.log("Invalid plot world: " + world);
                    } else {
                        final Plot plot = MainUtil.getPlot(world, id);
                        if (plot == null) {
                            PlotSquared.log("Could not find plot " + args[0] + " in world " + world);
                        } else {
                            MainUtil.clear(world, plot, true, null);
                            PlotSquared.log("Plot " + plot.getId().toString() + " cleared.");
                        }
                    }
                }
            }
            return true;
        }
        Location loc = plr.getLocation();
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!MainUtil.getTopPlot(loc.getWorld(), plot).equals(MainUtil.getBottomPlot( plot))) {
            return sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr))) && !Permissions.hasPermission(plr, "plots.admin.command.clear")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        assert plot != null;
        final long start = System.currentTimeMillis();
        boolean result = MainUtil.clearAsPlayer(plot, false, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
            }
        });
        if (!result) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
        }
        // sign
        // wall
        return result;
    }
}
