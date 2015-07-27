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
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Set;

@CommandDeclaration(
        command = "clear",
        description = "Clear a plot",
        permission = "plots.clear",
        category = CommandCategory.ACTIONS,
        usage = "/plot clear [id]"
)
public class Clear extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String ... args) {
        final Location loc = plr.getLocation();
        final Plot plot;
        if (args.length == 2) {
            PlotId id = PlotId.fromString(args[0]);
            if (id == null) {
                if (args[1].equalsIgnoreCase("mine")) {
                    Set<Plot> plots = PS.get().getPlots(plr);
                    if (plots.size() == 0) {
                        MainUtil.sendMessage(plr, C.NO_PLOTS);
                        return false;
                    }
                    plot = plots.iterator().next();
                }
                else {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
                    return false;
                }
            }
            else {
                plot = MainUtil.getPlot(loc.getWorld(), id);
            }
        }
        else {
            plot = MainUtil.getPlot(loc);
        }
        if (plot == null) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!MainUtil.getTopPlot(plot).equals(MainUtil.getBottomPlot(plot))) {
            return sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if (((plot == null) || !plot.hasOwner() || !plot.isOwner(UUIDHandler.getUUID(plr))) && !Permissions.hasPermission(plr, "plots.admin.command.clear")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        assert plot != null;
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                final boolean result = MainUtil.clearAsPlayer(plot, plot.owner == null, new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                }
            }
        };
        if (Settings.CONFIRM_CLEAR && !(Permissions.hasPermission(plr, "plots.confirm.bypass"))) {
            CmdConfirm.addPending(plr, "/plot clear " + plot.id, runnable);
        }
        else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
