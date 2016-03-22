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
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "bo3",
aliases = { "bo2" },
description = "Mark a plot as done",
permission = "plots.bo3",
category = CommandCategory.SCHEMATIC,
requiredType = RequiredType.NONE)
public class BO3 extends SubCommand {

    public void noArgs(final PlotPlayer plr) {
        MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot bo3 export [category] [alias] [-r]");
        MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot bo3 import <file>");
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot = loc.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((!plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.bo3")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length == 0) {
            noArgs(plr);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "output":
            case "save":
            case "export": {
                return BO3Handler.saveBO3(plr, plot);
            }
            case "paste":
            case "load":
            case "import":
            case "input": {
                // TODO NOT IMPLEMENTED YET
                MainUtil.sendMessage(plr, "NOT IMPLEMENTED YET!!!");
                return false;
            }
            default: {
                noArgs(plr);
                return false;
            }
        }
    }
}
