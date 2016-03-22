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
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.Set;

@CommandDeclaration(command = "clear",
description = "Clear a plot",
permission = "plots.clear",
category = CommandCategory.APPEARANCE,
usage = "/plot clear [id]",
aliases = "reset")
public class Clear extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String... args) {
        final Location loc = plr.getLocation();
        final Plot plot;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mine")) {
                Set<Plot> plots = plr.getPlots();
                if (!plots.isEmpty()) {
                    plot = plots.iterator().next();
                } else {
                    MainUtil.sendMessage(plr, C.NO_PLOTS);
                    return false;
                }
            } else {
                plot = MainUtil.getPlotFromString(plr, args[0], true);
            }
            if (plot == null) {
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
                return false;
            }
        } else if (args.length == 0) {
            plot = loc.getPlotAbs();
            if (plot == null) {
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
                C.NOT_IN_PLOT.send(plr);
                return false;
            }
        } else {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
            return false;
        }
        if ((!plot.hasOwner() || !plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.clear")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        if (plot.getRunning() != 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        if ((FlagManager.getPlotFlagRaw(plot, "done") != null)
        && (!Permissions.hasPermission(plr, "plots.continue") || (Settings.DONE_COUNTS_TOWARDS_LIMIT && (plr.getAllowedPlots() >= plr.getPlotCount())))) {
            MainUtil.sendMessage(plr, C.DONE_ALREADY_DONE);
            return false;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                final boolean result = plot.clear(true, false, new Runnable() {
                    @Override
                    public void run() {
                        plot.unlink();
                        SetQueue.IMP.addTask(new Runnable() {
                            @Override
                            public void run() {
                                plot.removeRunning();
                                // If the state changes, then mark it as no longer done
                                if (FlagManager.getPlotFlagRaw(plot, "done") != null) {
                                    FlagManager.removePlotFlag(plot, "done");
                                }
                                if (FlagManager.getPlotFlagRaw(plot, "analysis") != null) {
                                    FlagManager.removePlotFlag(plot, "analysis");
                                }
                                MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                            }
                        });
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                }
                else {
                    plot.addRunning();
                }
            }
        };
        if (Settings.CONFIRM_CLEAR && !(Permissions.hasPermission(plr, "plots.confirm.bypass"))) {
            CmdConfirm.addPending(plr, "/plot clear " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
