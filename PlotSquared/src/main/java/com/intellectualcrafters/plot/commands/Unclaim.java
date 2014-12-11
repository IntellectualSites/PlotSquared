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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Unclaim extends SubCommand {

    public Unclaim() {
        super(Command.UNCLAIM, "Unclaim a plot", "unclaim", CommandCategory.ACTIONS, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr == null) {
            // Is console
            if (args.length < 2) {
                PlotMain.sendConsoleSenderMessage("You need to specify two arguments: ID (0;0) & World (world)");
            } else {
                PlotId id = PlotId.fromString(args[0]);
                String world = args[1];
                if (id == null) {
                    PlotMain.sendConsoleSenderMessage("Invalid Plot ID: " + args[0]);
                } else {
                    if (!PlotMain.isPlotWorld(world)) {
                        PlotMain.sendConsoleSenderMessage("Invalid plot world: " + world);
                    } else {
                        Plot plot = PlotHelper.getPlot(Bukkit.getWorld(world), id);
                        if (plot == null) {
                            PlotMain.sendConsoleSenderMessage("Could not find plot " + args[0] + " in world " + world);
                        } else {
                            PlotMain.sendConsoleSenderMessage("Plot " + plot.getId().toString() + " cleared.");
                        }
                    }
                }
            }
            return true;
        }

        if (!PlayerFunctions.isInPlot(plr)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!PlayerFunctions.getTopPlot(plr.getWorld(), plot).equals(PlayerFunctions.getBottomPlot(plr.getWorld(), plot))) {
            return sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(plr.getUniqueId())) && !PlotMain.hasPermission(plr, "plots.admin")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        return true;
    }
}
