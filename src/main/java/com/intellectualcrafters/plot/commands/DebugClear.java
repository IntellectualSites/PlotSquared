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
import com.intellectualcrafters.plot.generator.SquarePlotWorld;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualcrafters.plot.object.PlotPlayer;

@CommandDeclaration(
        command = "debugclear",
        aliases = {"fastclear"},
        description = "Clear a plot using a fast experiment algorithm",
        category = CommandCategory.DEBUG
)
public class DebugClear extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if ((plot == null) || !(PS.get().getPlotWorld(loc.getWorld()) instanceof SquarePlotWorld)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!MainUtil.getTopPlot(plot).equals(MainUtil.getBottomPlot(plot))) {
            return sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if ((!plot.hasOwner() || !plot.isOwner(UUIDHandler.getUUID(plr))) && !Permissions.hasPermission(plr, "plots.admin.command.debugclear")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        final Location pos1 = MainUtil.getPlotBottomLoc(loc.getWorld(), plot.id).add(1, 0, 1);
        final Location pos2 = MainUtil.getPlotTopLoc(loc.getWorld(), plot.id);
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        MainUtil.runners.put(plot, 1);
        ChunkManager.manager.regenerateRegion(pos1, pos2, new Runnable() {
            @Override
            public void run() {
                MainUtil.runners.remove(plot);
                MainUtil.sendMessage(plr, "&aDone!");
            }
        });
        return true;
    }
}
