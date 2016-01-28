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

@CommandDeclaration(usage = "/plot swap <X;Z>", command = "swap", description = "Swap two plots", aliases = { "switch" }, category = CommandCategory.ACTIONS, requiredType = RequiredType.NONE)
public class Swap extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot1 = MainUtil.getPlotAbs(loc);
        if (plot1 == null) {
            return !MainUtil.sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot1.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, C.PERMISSION_ADMIN.s())) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        final Plot plot2 = MainUtil.getPlotFromString(plr, args[0], true);
        if ((plot2 == null)) {
            return false;
        }
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot copy <X;Z>");
            return false;
        }
        if (!plot1.getWorld().isCompatible(plot2.getWorld())) {
            C.PLOTWORLD_INCOMPATIBLE.send(plr);
            return false;
        }
        if (MainUtil.move(plot1, plot2, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, C.SWAP_SUCCESS);
            }
        }, true)) {
            return true;
        } else {
            MainUtil.sendMessage(plr, C.SWAP_OVERLAP);
            return false;
        }
    }
}
