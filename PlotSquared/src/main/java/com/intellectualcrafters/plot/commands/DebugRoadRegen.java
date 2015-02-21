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

import java.util.Arrays;

import org.bukkit.Chunk;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.SetBlockManager;

public class DebugRoadRegen extends SubCommand {
    public DebugRoadRegen() {
        super(Command.DEBUGROADREGEN, "Regenerate all road schematic in your current chunk", "debugroadregen", CommandCategory.DEBUG, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer player, final String... args) {
        if (!(PlotSquared.getPlotWorld(player.getWorld()) instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        final HybridPlotManager manager = (HybridPlotManager) PlotSquared.getPlotManager(player.getWorld());
        final Chunk chunk = BukkitUtil.getLocation(entity).getChunk();
        final boolean result = manager.regenerateRoad(chunk);
        if (result) {
            SetBlockManager.setBlockManager.update(Arrays.asList(new Chunk[] { chunk }));
        }
        MainUtil.sendMessage(BukkitUtil.getPlayer(player), "&6Regenerating chunk: " + chunk.getX() + "," + chunk.getZ() + "\n&6 - Result: " + (result == true ? "&aSuccess" : "&cFailed"));
        return true;
    }
}
