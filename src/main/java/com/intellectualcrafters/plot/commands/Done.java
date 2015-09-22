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
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "done", aliases = { "submit" }, description = "Mark a plot as done", permission = "plots.done", category = CommandCategory.ACTIONS, requiredType = RequiredType.NONE)
public class Done extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlotAbs(loc);
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((!plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.done")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getFlags().containsKey("done")) {
            MainUtil.sendMessage(plr, C.DONE_ALREADY_DONE);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        MainUtil.runners.put(plot, 1);
        MainUtil.sendMessage(plr, C.GENERATING_LINK);
        HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
            @Override
            public void run() {
                MainUtil.runners.remove(plot);
                if ((value == null) || (value.getComplexity() >= Settings.CLEAR_THRESHOLD)) {
                    final Flag flag = new Flag(FlagManager.getFlag("done"), (System.currentTimeMillis() / 1000));
                    FlagManager.addPlotFlag(plot, flag);
                    MainUtil.sendMessage(plr, C.DONE_SUCCESS);
                } else {
                    MainUtil.sendMessage(plr, C.DONE_INSUFFICIENT_COMPLEXITY);
                }
            }
        });
        return true;
    }
}
