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
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.TaskManager;

/**
 * Created 2014-08-01 for PlotSquared
 *
 * @author Empire92
 */
public class Move extends SubCommand {

    public Move() {
        super("move", "plots.admin", "plot moving debug test", "move", "condense", CommandCategory.DEBUG, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr == null) {
            PlayerFunctions.sendMessage(plr, "MUST BE EXECUTED BY PLAYER");
        }
        World world = plr.getWorld();
        PlotId plot1 = PlotHelper.parseId(args[0]);
        PlotId plot2 = PlotHelper.parseId(args[1]);
        if (plot1 == null || plot2 == null) {
            PlayerFunctions.sendMessage(plr, "INVALID PLOT ID\n/plot move <pos1> <pos2>");
        }
        if (plot1 == plot2) {
            PlayerFunctions.sendMessage(plr, "DUPLICATE ID");
        }
        if (move(world, plot1, plot2, null)) {
            PlayerFunctions.sendMessage(plr, "MOVE SUCCESS");
        }
        else {
            PlayerFunctions.sendMessage(plr, "MOVE FAILED");
        }
        return true;
    }
    
    public boolean move(final World world, final PlotId current, PlotId newPlot, final Runnable whenDone) {
        final Location bot1 = PlotHelper.getPlotBottomLoc(world, current);
        Location bot2 = PlotHelper.getPlotBottomLoc(world, newPlot);
        final Location top = PlotHelper.getPlotTopLoc(world, current);
        final Plot currentPlot = PlotHelper.getPlot(world, current);
        if (currentPlot.owner == null) {
            return false;
        }
        Plot pos1 = PlayerFunctions.getBottomPlot(world, currentPlot); 
        Plot pos2 = PlayerFunctions.getTopPlot(world, currentPlot);
        PlotId size = PlotHelper.getSize(world, currentPlot);
        if (!PlotHelper.isUnowned(world, newPlot, new PlotId(newPlot.x + size.x - 1, newPlot.y + size.y - 1))) {
            return false;
        }
        
        int offset_x = newPlot.x - current.x;
        int offset_y = newPlot.y - current.y;
        final ArrayList<PlotId> selection = PlayerFunctions.getPlotSelectionIds(pos1.id, pos2.id);
        String worldname = world.getName();
        for (PlotId id : selection) { 
            DBFunc.movePlot(world.getName(), new PlotId(id.x, id.y), new PlotId(id.x + offset_x, id.y + offset_y));
            Plot plot = PlotMain.getPlots(worldname).get(id);
            PlotMain.getPlots(worldname).remove(id);
            plot.id.x += offset_x;
            plot.id.y += offset_y;
            PlotMain.getPlots(worldname).put(plot.id, plot);
        }
        ChunkManager.copyRegion(bot1, top, bot2, new Runnable() {
            @Override
            public void run() {
                ChunkManager.regenerateRegion(bot1, top, null);
                TaskManager.runTaskLater(whenDone, 1);
            }
        });
        return true;
    }
}
