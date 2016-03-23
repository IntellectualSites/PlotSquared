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
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "debugroadregen",
        usage = "/plot debugroadregen",
        requiredType = RequiredType.NONE,
        description = "Regenerate all roads based on the road schematic",
        category = CommandCategory.DEBUG,
        permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String... args) {
        Location loc = player.getLocation();
        PlotArea plotworld = loc.getPlotArea();
        if (!(plotworld instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            ChunkLoc chunk = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
            int extend = 0;
            if (args.length == 1) {
                if (MathMan.isInteger(args[0])) {
                    try {
                        extend = Integer.parseInt(args[0]);
                    } catch (Exception e) {
                        C.NOT_VALID_NUMBER.send(player, "(0, <EXTEND HEIGHT>)");
                        return false;
                    }
                }
            }
            boolean result = HybridUtils.manager.regenerateRoad(plotworld, chunk, extend);
            MainUtil.sendMessage(player,
                    "&6Regenerating chunk: " + chunk.x + "," + chunk.z + "\n&6 - Result: " + (result ? "&aSuccess" : "&cFailed"));
            MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        } else {
            HybridPlotManager manager = (HybridPlotManager) plotworld.getPlotManager();
            manager.createRoadEast(plotworld, plot);
            manager.createRoadSouth(plotworld, plot);
            manager.createRoadSouthEast(plotworld, plot);
            MainUtil.sendMessage(player, "&6Regenerating plot south/east roads: " + plot.getId() + "\n&6 - Result: &aSuccess");
            MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        }
        return true;
    }
}
