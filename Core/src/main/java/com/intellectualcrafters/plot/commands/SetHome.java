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
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "sethome",
        permission = "plots.set.home",
        description = "Set the plot home",
        usage = "/plot sethome [none]",
        aliases = {"sh", "seth"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class SetHome extends SetCommand {

    @Override
    public boolean set(PlotPlayer plr, Plot plot, String value) {
        switch (value.toLowerCase()) {
            case "unset":
            case "remove":
            case "none": {
                Plot base = plot.getBasePlot(false);
                base.setHome(null);
                return MainUtil.sendMessage(plr, C.POSITION_UNSET);
            }
            case "": {
                Plot base = plot.getBasePlot(false);
                Location bot = base.getBottomAbs();
                Location loc = plr.getLocationFull();
                BlockLoc rel = new BlockLoc(loc.getX() - bot.getX(), loc.getY(), loc.getZ() - bot.getZ(), loc.getYaw(), loc.getPitch());
                base.setHome(rel);
                return MainUtil.sendMessage(plr, C.POSITION_SET);
            }
            default: {
                MainUtil.sendMessage(plr, C.HOME_ARGUMENT);
                return false;
            }
        }
    }
}
