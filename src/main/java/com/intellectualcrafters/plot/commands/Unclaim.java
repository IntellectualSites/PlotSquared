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
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

/**
 * Unclaiming a plot makes no changes to the terrain or plot border and only removes the owner and should be regarded as an admin command. 
 */
@CommandDeclaration(
command = "unclaim",
usage = "/plot unclaim",
requiredType = RequiredType.NONE,
description = "Unclaim a plot (admin command/does not clear plot)",
category = CommandCategory.ACTIONS)
public class Unclaim extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlotAbs(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(plr, C.PLOT_NOT_CLAIMED);
        }
        if (((!plot.hasOwner() || !plot.isOwner(plr.getUUID()))) && !Permissions.hasPermission(plr, "plots.admin.command.unclaim")) {
            return !sendMessage(plr, C.NO_PLOT_PERMS);
        }
        if (plot.unclaim()) {
            MainUtil.sendMessage(plr, C.UNCLAIM_SUCCESS);
        } else {
            MainUtil.sendMessage(plr, C.UNCLAIM_FAILED);
        }
        return true;
    }
}
