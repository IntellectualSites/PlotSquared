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

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

public class DebugRoadRegen extends SubCommand {
    public DebugRoadRegen() {
        super(Command.DEBUGROADREGEN, "Regenerate all road schematic in your current chunk", "debugroadregen", CommandCategory.DEBUG, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer player, final String... args) {
        Location loc = player.getLocation();
        String world = loc.getWorld();
        if (!(PlotSquared.getPlotWorld(world) instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        ChunkLoc chunk = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
        boolean result = HybridUtils.manager.regenerateRoad(world, chunk);
        if (result) {
            MainUtil.update(loc);
        }
        MainUtil.sendMessage(player, "&6Regenerating chunk: " + chunk.x + "," + chunk.z + "\n&6 - Result: " + (result == true ? "&aSuccess" : "&cFailed"));
        return true;
    }
}
