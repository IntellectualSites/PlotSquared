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
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.List;

@CommandDeclaration(
        command = "regenallroads",
        description = "Regenerate all roads in the map using the set road schematic",
        aliases = {"rgar"},
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.CONSOLE,
        permission = "plots.regenallroads"
)
public class RegenAllRoads extends SubCommand {

    public RegenAllRoads() {
        requiredArguments = new Argument[] {
                Argument.String
        };
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        int height = 0;
        if (args.length == 2) {
            try {
                height = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                MainUtil.sendMessage(plr, C.NOT_VALID_NUMBER, "(0, 256)");
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot regenallroads <world> [height]");
                return false;
            }
        }
        final String name = args[0];
        final PlotManager manager = PS.get().getPlotManager(name);
        if ((manager == null) || !(manager instanceof HybridPlotManager)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD);
            return false;
        }
        final List<ChunkLoc> chunks = ChunkManager.manager.getChunkChunks(name);
        PS.log("&cIf no schematic is set, the following will not do anything");
        PS.log("&7 - To set a schematic, stand in a plot and use &c/plot createroadschematic");
        PS.log("&6Potential chunks to update: &7" + (chunks.size() * 1024));
        PS.log("&6Estimated time: &7" + (chunks.size()) + " seconds");
        final boolean result = HybridUtils.manager.scheduleRoadUpdate(name, height);
        if (!result) {
            PS.log("&cCannot schedule mass schematic update! (Is one already in progress?)");
            return false;
        }
        return true;
    }
}
