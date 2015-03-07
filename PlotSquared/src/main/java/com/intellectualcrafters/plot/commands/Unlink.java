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

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * Created 2014-08-01 for PlotSquared
 *
 * @author Citymonstret
 */
public class Unlink extends SubCommand {
    public Unlink() {
        super(Command.UNLINK, "Unlink a mega-plot", "unlink", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr))) && !Permissions.hasPermission(plr, "plots.admin.command.unlink")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        if (MainUtil.getTopPlot(plot).equals(MainUtil.getBottomPlot(plot))) {
            return sendMessage(plr, C.UNLINK_IMPOSSIBLE);
        }
        
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!unlinkPlot(plot)) {
                    MainUtil.sendMessage(plr, "&cUnlink has been cancelled");
                    return;
                }
                MainUtil.update(plr.getLocation());
                MainUtil.sendMessage(plr, C.UNLINK_SUCCESS);
            }
        };
        if (Settings.CONFIRM_UNLINK) {
            CmdConfirm.addPending(plr, "/plot unlink " + plot.id, runnable);
        }
        else {
            TaskManager.runTask(runnable);
        }
        return true;
    }

    public static boolean unlinkPlot(final Plot plot) {
        final String world = plot.world;
        final PlotId pos1 = MainUtil.getBottomPlot(plot).id;
        final PlotId pos2 = MainUtil.getTopPlot(plot).id;
        final ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(pos1, pos2);
        final boolean result = EventUtil.manager.callUnlink(world, ids);
        if (!result) {
            return false;
        }
        final PlotManager manager = PlotSquared.getPlotManager(world);
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        manager.startPlotUnlink(plotworld, ids);
        for (final PlotId id : ids) {
            final Plot myplot = PlotSquared.getPlots(world).get(id);
            if (plot == null) {
                continue;
            }
            if (plot.helpers != null) {
                myplot.helpers = plot.helpers;
            }
            if (plot.denied != null) {
                myplot.denied = plot.denied;
            }
            myplot.deny_entry = plot.deny_entry;
            myplot.settings.setMerged(new boolean[] { false, false, false, false });
            DBFunc.setMerged(world, myplot, myplot.settings.getMerged());
        }
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final boolean lx = x < pos2.x;
                final boolean ly = y < pos2.y;
                final Plot p = MainUtil.getPlot(world, new PlotId(x, y));
                if (lx) {
                    manager.createRoadEast(plotworld, p);
                    if (ly) {
                        manager.createRoadSouthEast(plotworld, p);
                    }
                }
                if (ly) {
                    manager.createRoadSouth(plotworld, p);
                }
                MainUtil.setSign(UUIDHandler.getName(plot.owner), plot);
            }
        }
        manager.finishPlotUnlink(plotworld, ids);
        for (final PlotId id : ids) {
            final Plot myPlot = MainUtil.getPlot(world, id);
            if (plot.hasOwner()) {
                final String name = UUIDHandler.getName(myPlot.owner);
                if (name != null) {
                    MainUtil.setSign(name, myPlot);
                }
            }
        }
        return true;
    }
}
