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
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "debugroadregen",
        usage = "/plot debugroadregen",
        requiredType = RequiredType.NONE,
        description = "Regenerate all roads based on the road schematic",
        category = CommandCategory.DEBUG,
        permission = "plots.debugroadregen"
)
public class DebugRoadRegen extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, final String ... args) {
        final Location loc = player.getLocation();
        final String world = loc.getWorld();
        PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (!(plotworld instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            final ChunkLoc chunk = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
            boolean result = HybridUtils.manager.regenerateRoad(world, chunk, 0);
            MainUtil.sendMessage(player, "&6Regenerating chunk: " + chunk.x + "," + chunk.z + "\n&6 - Result: " + (result == true ? "&aSuccess" : "&cFailed"));
        }
        else {
            HybridPlotManager manager = (HybridPlotManager) PS.get().getPlotManager(world);
            manager.createRoadEast(plotworld, plot);
            manager.createRoadSouth(plotworld, plot);
            manager.createRoadSouthEast(plotworld, plot);
            MainUtil.sendMessage(player, "&6Regenerating plot south/east roads: " + plot.id + "\n&6 - Result: &aSuccess");
        }
        return true;
    }
}
