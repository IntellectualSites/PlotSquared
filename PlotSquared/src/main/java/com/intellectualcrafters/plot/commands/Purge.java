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
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import org.bukkit.entity.Player;

@SuppressWarnings({"unused", "deprecated", "javadoc"})
public class Purge extends SubCommand {

    public Purge() {
        super("purge", "plots.admin", "Purge all plots for a world", "purge", "", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length != 2) {
            if (args.length == 1) {
                try {
                    final String[] split = args[0].split(";");
                    final String world = split[0];
                    final PlotId id = new PlotId(Integer.parseInt(split[1]), Integer.parseInt(split[2]));

                    System.out.print("VALID ID");

                    if (plr != null) {
                        PlayerFunctions.sendMessage(plr, (C.NOT_CONSOLE));
                        return false;
                    }

                    if (!PlotMain.isPlotWorld(world)) {
                        PlayerFunctions.sendMessage(null, C.NOT_VALID_PLOT_WORLD);
                        return false;
                    }
                    PlotMain.getPlots(world).remove(id);
                    DBFunc.purge(world, id);
                    PlayerFunctions.sendMessage(null, "&aPurge of '" + args[0] + "' was successful!");
                    return true;
                } catch (final Exception e) {
                    PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_ID);
                }
            }
            PlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
            return false;
        }
        if (args[1].equals("-o")) {
            if (PlotMain.getPlots(args[0]) == null) {
                PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_WORLD);
                return false;
            }
            PlotMain.removePlotWorld(args[0]);
            DBFunc.purge(args[0]);
            PlayerFunctions.sendMessage(plr, (C.PURGE_SUCCESS));
            return true;
        } else {
            PlayerFunctions.sendMessage(plr, "This is a dangerous command, if you are sure, use /plot purge {world} -o");
            return false;
        }
    }

}
