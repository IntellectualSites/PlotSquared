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

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Visit extends SubCommand {
    public Visit() {
        super("visit", "plots.visit", "Visit someones plot", "visit {player} [#]", "v", CommandCategory.TELEPORT, true);
    }

    public List<Plot> getPlots(final UUID uuid) {
        final List<Plot> plots = new ArrayList<>();
        for (final Plot p : PlotSquared.getPlots()) {
            if (p.hasOwner() && p.isOwner(uuid)) {
                plots.add(p);
            }
        }
        return plots;
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            return sendMessage(plr, C.COMMAND_SYNTAX, "/plot visit <player|alias|world|id> [#]");
        }
        ArrayList<Plot> plots = new ArrayList<>();
        UUID user = UUIDHandler.getUUID(args[0]);
        if (user != null ) {
            // do plots by username
            plots.addAll(PlotSquared.getPlots(user));
        }
        else if (PlotSquared.isPlotWorld(args[0])) {
            // do plots by world
            plots.addAll(PlotSquared.getPlots(args[0]).values());
        }
        else {
            Plot plot = MainUtil.getPlotFromString(plr, args[0], false);
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
        
//        
//        // from alias
//        
//        
//        id = PlotId.fromString(args[0]);
//        
//        
//        
//        final String username = args[0];
//        final UUID uuid = UUIDHandler.getUUID(username);
//        List<Plot> plots = null;
//        if (uuid != null) {
//            plots = PlotSquared.sortPlotsByWorld(getPlots(uuid));
//        }
//        if ((uuid == null) || plots.isEmpty()) {
//            return sendMessage(plr, C.FOUND_NO_PLOTS);
//        }
//        if (args.length < 2) {
//            MainUtil.teleportPlayer(plr, plr.getLocation(), plots.get(0));
//            return true;
//        }
//        int i;
//        try {
//            i = Integer.parseInt(args[1]);
//        } catch (final Exception e) {
//            return sendMessage(plr, C.NOT_VALID_NUMBER);
//        }
//        if ((i < 1) || (i > plots.size())) {
//            return sendMessage(plr, C.NOT_VALID_NUMBER);
//        }
//        MainUtil.teleportPlayer(plr, plr.getLocation(), plots.get(i - 1));
//        return true;
    }
}
