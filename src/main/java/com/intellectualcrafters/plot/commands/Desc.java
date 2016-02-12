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
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "setdescription",
permission = "plots.set.desc",
description = "Set the plot description",
usage = "/plot desc <description>",
aliases = { "desc", "setdesc", "setd", "description" },
 category = CommandCategory.SETTINGS,
requiredType = RequiredType.NONE)
public class Desc extends SetCommand {
    
    @Override
    public boolean set(PlotPlayer plr, Plot plot, String desc) {
        if (desc.length() == 0) {
            plot.removeFlag("description");
            MainUtil.sendMessage(plr, C.DESC_UNSET);
            return true;
        }
        final Flag flag = new Flag(FlagManager.getFlag("description"), desc);
        final boolean result = FlagManager.addPlotFlag(plot, flag);
        if (!result) {
            MainUtil.sendMessage(plr, C.FLAG_NOT_ADDED);
            return false;
        }
        MainUtil.sendMessage(plr, C.DESC_SET);
        return true;
    }
}
