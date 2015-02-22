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

import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

/**
 * Created 2014-08-01 for PlotSquared
 *
 * @author Empire92
 */
public class Move extends SubCommand {
    public Move() {
        super("debugmove", "plots.admin", "plot moving debug test", "debugmove", "move", CommandCategory.DEBUG, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr == null) {
            MainUtil.sendMessage(plr, "MUST BE EXECUTED BY PLAYER");
        }
        if (args.length != 2) {
            MainUtil.sendMessage(plr, "/plot move <pos1> <pos2>");
            return false;
        }
        final String world = plr.getLocation().getWorld();
        final PlotId plot1 = MainUtil.parseId(args[0]);
        final PlotId plot2 = MainUtil.parseId(args[1]);
        if ((plot1 == null) || (plot2 == null)) {
            MainUtil.sendMessage(plr, "INVALID PLOT ID\n/plot move <pos1> <pos2>");
            return false;
        }
        if (plot1 == plot2) {
            MainUtil.sendMessage(plr, "DUPLICATE ID");
            return false;
        }
        if (MainUtil.move(world, plot1, plot2, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, "MOVE SUCCESS");
            }
        })) {
            return true;
        } else {
            MainUtil.sendMessage(plr, "MOVE FAILED");
            return false;
        }
    }
}
