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
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "delete",
permission = "plots.delete",
description = "Delete a plot",
usage = "/plot delete",
aliases = { "dispose", "del" },
category = CommandCategory.ACTIONS,
requiredType = RequiredType.NONE)
public class Delete extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!MainUtil.getTopPlot(plot).equals(MainUtil.getBottomPlot(plot))) {
            return !sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if (((!plot.hasOwner() || !plot.isOwner(plr.getUUID()))) && !Permissions.hasPermission(plr, "plots.admin.command.delete")) {
            return !sendMessage(plr, C.NO_PLOT_PERMS);
        }
        final PlotWorld pWorld = PS.get().getPlotWorld(plot.world);
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if ((EconHandler.manager != null) && pWorld.USE_ECONOMY && plot.hasOwner() && plot.isOwner(UUIDHandler.getUUID(plr))) {
                    final double c = pWorld.SELL_PRICE;
                    if (c > 0d) {
                        EconHandler.manager.depositMoney(plr, c);
                        sendMessage(plr, C.ADDED_BALANCE, c + "");
                    }
                }
                if (plot.unclaim()) {
                    final long start = System.currentTimeMillis();
                    final boolean result = MainUtil.clearAsPlayer(plot, true, new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                        }
                    });
                    if (!result) {
                        MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                    }
                } else {
                    MainUtil.sendMessage(plr, C.UNCLAIM_FAILED);
                }
            }
        };
        if (Settings.CONFIRM_DELETE && !(Permissions.hasPermission(plr, "plots.confirm.bypass"))) {
            CmdConfirm.addPending(plr, "/plot delete " + plot.id, runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
