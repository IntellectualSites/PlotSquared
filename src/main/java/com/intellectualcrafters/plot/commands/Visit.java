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
import java.util.List;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "visit",
        permission = "plots.visit",
        description = "Visit someones plot",
        usage = "/plot visit <player|aliases|world|id> [#]",
        aliases = {"v"},
        requiredType = RequiredType.NONE,
        category = CommandCategory.TELEPORT
)
public class Visit extends SubCommand {

    public Visit() {
        requiredArguments = new Argument[] {
                Argument.String
        };
    }
    
    public List<Plot> getPlots(final UUID uuid) {
        final List<Plot> plots = new ArrayList<>();
        for (final Plot p : PS.get().getPlots()) {
            if (p.hasOwner() && p.isOwner(uuid)) {
                plots.add(p);
            }
        }
        return plots;
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        ArrayList<Plot> plots = new ArrayList<>();
        UUID user = UUIDHandler.getCachedUUID(args[0], null);
        if (user != null ) {
            // do plots by username
            plots = PS.get().sortPlots(PS.get().getPlots(user), null);
        } else if (PS.get().isPlotWorld(args[0])) {
            // do plots by world
            plots = PS.get().sortPlots(PS.get().getPlots(args[0]).values(), null);
        }
        else {
            Plot plot = MainUtil.getPlotFromString(plr, args[0], true);
            if (plot == null) {
                return false;
            }
            plots.add(plot);
        }
        if (plots.size() == 0) {
            sendMessage(plr, C.FOUND_NO_PLOTS);
            return false;
        }
        int index = 0;
        if (args.length == 2) {
            try {
                index = Integer.parseInt(args[1]) - 1;
                if (index < 0 || index >= plots.size()) {
                    sendMessage(plr, C.NOT_VALID_NUMBER, "(1, " + plots.size() + ")");
                    sendMessage(plr, C.COMMAND_SYNTAX, "/plot visit " + args[0] + " [#]");
                    return false;
                }
            }
            catch (Exception e) {
                sendMessage(plr, C.NOT_VALID_NUMBER, "(1, " + plots.size() + ")");
                sendMessage(plr, C.COMMAND_SYNTAX, "/plot visit " + args[0] + " [#]");
                return false;
            }
        }

        Plot plot = plots.get(index);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(plr, "plots.visit.unowned")) {
                sendMessage(plr, C.NO_PERMISSION, "plots.visit.unowned");
                return false;
            }
        }
        else if (plot.isOwner(plr.getUUID())) {
            if (!Permissions.hasPermission(plr, "plots.visit.owned") && !Permissions.hasPermission(plr, "plots.home")) {
                sendMessage(plr, C.NO_PERMISSION, "plots.visit.owned, plots.home");
                return false;
            }
        }
        else if (plot.isAdded(plr.getUUID())) {
            if (!Permissions.hasPermission(plr, "plots.visit.shared")) {
                sendMessage(plr, C.NO_PERMISSION, "plots.visit.shared");
                return false;
            }
        }
        else {
            if (!Permissions.hasPermission(plr, "plots.visit.other")) {
                sendMessage(plr, C.NO_PERMISSION, "plots.visit.other");
                return false;
            }
        }
        MainUtil.teleportPlayer(plr, plr.getLocation(), plots.get(index));
        return true;
    }

}
