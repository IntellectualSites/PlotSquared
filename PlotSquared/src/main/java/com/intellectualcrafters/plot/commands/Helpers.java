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

import java.util.UUID;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Helpers extends SubCommand {
    public Helpers() {
        super(Command.HELPERS, "Manage plot helpers", "helpers {add|remove} {player}", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 2) {
            MainUtil.sendMessage(plr, C.HELPER_NEED_ARGUMENT);
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
        if (!plot.getOwner().equals(UUIDHandler.getUUID(plr)) && !Permissions.hasPermission(plr, "plots.admin.command.helpers")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            UUID uuid;
            if (args[1].equalsIgnoreCase("*")) {
                uuid = DBFunc.everyone;
            } else {
                uuid = UUIDHandler.getUUID(args[1]);
            }
            if (uuid == null) {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[1]);
                return false;
            }
            if (!plot.helpers.contains(uuid)) {
                if (plot.owner.equals(uuid)) {
                    MainUtil.sendMessage(plr, C.ALREADY_OWNER);
                    return false;
                }
                if (plot.trusted.contains(uuid)) {
                    plot.trusted.remove(uuid);
                    DBFunc.removeTrusted(loc.getWorld(), plot, uuid);
                }
                if (plot.denied.contains(uuid)) {
                    plot.denied.remove(uuid);
                    DBFunc.removeDenied(loc.getWorld(), plot, uuid);
                }
                plot.addHelper(uuid);
                DBFunc.setHelper(loc.getWorld(), plot, uuid);
                EventUtil.manager.callHelper(plr, plot, uuid, true);
            } else {
                MainUtil.sendMessage(plr, C.ALREADY_ADDED);
                return false;
            }
            MainUtil.sendMessage(plr, C.HELPER_ADDED);
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args[1].equalsIgnoreCase("*")) {
                final UUID uuid = DBFunc.everyone;
                if (!plot.helpers.contains(uuid)) {
                    MainUtil.sendMessage(plr, C.WAS_NOT_ADDED);
                    return true;
                }
                plot.removeHelper(uuid);
                DBFunc.removeHelper(loc.getWorld(), plot, uuid);
                MainUtil.sendMessage(plr, C.HELPER_REMOVED);
                return true;
            }
            final UUID uuid = UUIDHandler.getUUID(args[1]);
            plot.removeHelper(uuid);
            DBFunc.removeHelper(loc.getWorld(), plot, uuid);
            EventUtil.manager.callHelper(plr, plot, uuid, false);
            MainUtil.sendMessage(plr, C.HELPER_REMOVED);
        } else {
            MainUtil.sendMessage(plr, C.HELPER_NEED_ARGUMENT);
            return true;
        }
        return true;
    }
}
