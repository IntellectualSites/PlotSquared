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

import java.util.ArrayList;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "swap", description = "Swap two plots", aliases = { "switch" }, category = CommandCategory.ACTIONS, requiredType = RequiredType.NONE)
public class Swap extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        MainUtil.sendMessage(plr, "&cThis command has not been optimized for large selections yet. Please bug me if this becomes an issue.");
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.NEED_PLOT_ID);
            MainUtil.sendMessage(plr, C.SWAP_SYNTAX);
            return false;
        }
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((!plot.hasOwner() || !plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.swap")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        
        final Plot bot1 = MainUtil.getBottomPlot(plot);
        final Plot top1 = MainUtil.getTopPlot(plot);
        
        final PlotId id2 = PlotId.fromString(args[0]);
        if (id2 == null) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(plr, C.SWAP_SYNTAX);
            return false;
        }
        final String world = loc.getWorld();
        final Plot plot2 = MainUtil.getPlot(world, id2);
        final PlotId id3 = new PlotId((id2.x + top1.id.x) - bot1.id.x, (id2.y + top1.id.y) - bot1.id.y);
        final Plot plot3 = MainUtil.getPlot(world, id3);
        
        // Getting secon selection
        final Plot bot2 = MainUtil.getBottomPlot(plot2);
        final Plot top2 = MainUtil.getTopPlot(plot3);
        
        // cancel swap if intersection
        final PlotCluster cluster1 = new PlotCluster(world, bot1.id, top1.id, null);
        final PlotClusterId cluster2id = new PlotClusterId(bot2.id, top2.id);
        if (ClusterManager.intersects(cluster1, cluster2id)) {
            MainUtil.sendMessage(plr, C.SWAP_OVERLAP);
            return false;
        }
        
        // Check dimensions
        if (((top1.id.x - bot1.id.x) != (top2.id.x - bot2.id.x)) || ((top1.id.y - bot1.id.y) != (top2.id.y - bot2.id.y))) {
            MainUtil.sendMessage(plr, C.SWAP_DIMENSIONS, "1");
            MainUtil.sendMessage(plr, C.SWAP_SYNTAX);
            return false;
        }
        
        // Getting selections as ids
        final ArrayList<PlotId> selection1 = MainUtil.getPlotSelectionIds(bot1.id, top1.id);
        final ArrayList<PlotId> selection2 = MainUtil.getPlotSelectionIds(bot2.id, top2.id);
        
        // Getting selections as location coordinates
        final Location pos1 = MainUtil.getPlotBottomLocAbs(world, bot1.id);
        final Location pos2 = MainUtil.getPlotTopLocAbs(world, top1.id).subtract(1, 0, 1);
        final Location pos3 = MainUtil.getPlotBottomLocAbs(world, bot2.id);
        final Location pos4 = MainUtil.getPlotTopLocAbs(world, top2.id).subtract(1, 0, 1);
        
        if (MainUtil.getPlot(pos2) != null) {
            pos1.add(1, 0, 1);
            pos2.add(1, 0, 1);
            pos3.add(1, 0, 1);
            pos4.add(1, 0, 1);
        }
        
        // Swapping the blocks, states and entites
        ChunkManager.manager.swap(world, pos1, pos2, pos3, pos4);
        
        // Swapping the plot data
        for (int i = 0; i < selection1.size(); i++) {
            final boolean last = i == (selection1.size() - 1);
            final PlotId swaper = selection1.get(i);
            final PlotId swapee = selection2.get(i);
            MainUtil.swapData(world, swaper, swapee, new Runnable() {
                @Override
                public void run() {
                    if (last) {
                        MainUtil.sendMessage(plr, C.SWAP_SUCCESS);
                    }
                }
            });
        }
        MainUtil.sendMessage(plr, C.STARTED_SWAP);
        return true;
    }
}
