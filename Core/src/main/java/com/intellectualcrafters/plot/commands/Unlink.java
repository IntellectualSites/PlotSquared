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
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "unlink", aliases = { "u", "unmerge" }, description = "Unlink a mega-plot", usage = "/plot unlink", requiredType = RequiredType.NONE, category = CommandCategory.SETTINGS)
public class Unlink extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        final Location loc = plr.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(plr, C.PLOT_UNOWNED);
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.unlink")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        if (!plot.isMerged()) {
            return sendMessage(plr, C.UNLINK_IMPOSSIBLE);
        }
        final boolean createRoad;
        if (args.length != 0) {
            if (args.length != 1 || StringMan.isEqualIgnoreCaseToAny(args[1], "true", "false")) {
                C.COMMAND_SYNTAX.send(plr, getUsage());
                return false;
            }
            createRoad = Boolean.parseBoolean(args[1]);
        } else {
            createRoad = true;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!plot.unlinkPlot(createRoad, createRoad)) {
                    MainUtil.sendMessage(plr, "&cUnlink has been cancelled");
                    return;
                }
                MainUtil.sendMessage(plr, C.UNLINK_SUCCESS);
            }
        };
        if (Settings.CONFIRM_UNLINK && !(Permissions.hasPermission(plr, "plots.confirm.bypass"))) {
            CmdConfirm.addPending(plr, "/plot unlink " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
