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
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualcrafters.plot.object.PlotPlayer;

@CommandDeclaration(
        command = "move",
        description = "Move a plot",
        aliases = {"debugmove"},
        permission = "plots.move",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.PLAYER
)
public class Move extends SubCommand {

    public Move() {
        requiredArguments = new Argument[] {
                Argument.PlotID
        };
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        final Location loc = plr.getLocation();
        final Plot plot1 = MainUtil.getPlot(loc);
        if (plot1 == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot1.isAdded(plr.getUUID()) && !plr.hasPermission(Permissions.ADMIN.s))  {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        final String world = loc.getWorld();
        final PlotId plot2id = MainUtil.parseId(args[0]);
        if ((plot2id == null)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot move <X;Z>");
            return false;
        }
        String world2;
        if (args.length == 2) {
            PlotWorld other = PS.get().getPlotWorld(args[1]);
            PlotWorld current = PS.get().getPlotWorld(loc.getWorld());
            if (other == null || current == null || !other.equals(current)) {
                MainUtil.sendMessage(plr, C.PLOTWORLD_INCOMPATIBLE);
                return false;
            }
        	world2 = other.worldname;
        }
        else {
        	world2 = world;
        }
        Plot plot2 = MainUtil.getPlot(world2, plot2id);
        
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot move <X;Z>");
            return false;
        }
        if (MainUtil.move(plot1, plot2, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, C.MOVE_SUCCESS);
            }
        })) {
            return true;
        } else {
            MainUtil.sendMessage(plr, C.REQUIRES_UNOWNED);
            return false;
        }
    }
}
