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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.intellectualcrafters.plot.util.bukkit.uuid.SQLUUIDHandler;

public class Undeny extends SubCommand {
    public Undeny() {
        super(Command.UNDENY, "Remove a denied user from a plot", "undeny <player>", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length != 1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot undeny <player>");
            return true;
        }
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((plot == null) || !plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.undeny")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        int count = 0;
        if (args[0].equals("unknown")) {
            ArrayList<UUID> toRemove = new ArrayList<>();
            for (UUID uuid : plot.getDenied()) {
                if (UUIDHandler.getName(uuid) == null) {
                    toRemove.add(uuid);
                }
            }
            for (UUID uuid : toRemove) {
                plot.removeDenied(uuid);
                count++;
            }
        }
        else if (args[0].equals("*")){
            for (UUID uuid : new ArrayList<>(plot.getDenied())) {
                plot.removeDenied(uuid);
                count++;
            }
        }
        else {
            UUID uuid = UUIDHandler.getUUID(args[0]);
            if (uuid != null) {
                if (plot.removeDenied(uuid)) {
                    count++;   
                }
            }
        }
        if (count == 0) {
            if (UUIDHandler.implementation instanceof SQLUUIDHandler) {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER_WAIT, args[0]);
            } else {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            }
            return false;
        }
        else {
            MainUtil.sendMessage(plr, C.REMOVED_PLAYERS, count + "");
        }
        return true;
    }
}
