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
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "target",
        usage = "/plot target <<plot>|nearest>",
        description = "Target a plot with your compass",
        permission = "plots.target",
        requiredType = RequiredType.NONE,
        category = CommandCategory.INFO)
public class Target extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        Location ploc = plr.getLocation();
        if (!ploc.isPlotArea()) {
            MainUtil.sendMessage(plr, C.NOT_IN_PLOT_WORLD);
            return false;
        }
        Plot target = null;
        if (StringMan.isEqualIgnoreCaseToAny(args[0], "near", "nearest")) {
            int distance = Integer.MAX_VALUE;
            for (Plot plot : PS.get().getPlots(ploc.getWorld())) {
                double current = plot.getCenter().getEuclideanDistanceSquared(ploc);
                if (current < distance) {
                    distance = (int) current;
                    target = plot;
                }
            }
            if (target == null) {
                MainUtil.sendMessage(plr, C.FOUND_NO_PLOTS);
                return false;
            }
        } else if ((target = MainUtil.getPlotFromString(plr, args[0], true)) == null) {
            return false;
        }
        plr.setCompassTarget(target.getCenter());
        MainUtil.sendMessage(plr, C.COMPASS_TARGET);
        return true;
    }
}
