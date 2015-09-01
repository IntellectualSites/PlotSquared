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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "target",
        usage = "/plot target <X;Z|nearest>",
        description = "Target a plot with your compass",
        permission = "plots.target",
        requiredType = RequiredType.NONE,
        category = CommandCategory.ACTIONS
)
public class Target extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location ploc = plr.getLocation();
        if (!PS.get().isPlotWorld(ploc.getWorld())) {
            MainUtil.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
            return false;
        }
        PlotId id = MainUtil.parseId(args[0]);
        if (id == null) {
            if (StringMan.isEqualIgnoreCaseToAny(args[0], "near", "nearest")) {
                Plot closest = null;
                int distance = Integer.MAX_VALUE;
                for (Plot plot : PS.get().getPlotsInWorld(ploc.getWorld())) {
                    double current = plot.getBottom().getEuclideanDistanceSquared(ploc); 
                    if (current < distance) {
                        distance = (int) current;
                        closest = plot;
                    }
                }
                id = closest.id;
            }
            else {
                MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
                return false;
            }
        }
        final Location loc = MainUtil.getPlotHome(ploc.getWorld(), id);
        plr.setCompassTarget(loc);
        MainUtil.sendMessage(plr, C.COMPASS_TARGET);
        return true;
    }
}
