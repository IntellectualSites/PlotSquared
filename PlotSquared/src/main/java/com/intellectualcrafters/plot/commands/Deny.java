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

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

import java.util.UUID;

public class Deny extends SubCommand {
    public Deny() {
        super(Command.DENY, "Deny a user from a plot", "deny <player>", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length != 1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot deny <player>");
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
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.deny")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        UUID uuid;
        if (args[0].equalsIgnoreCase("*")) {
            uuid = DBFunc.everyone;
        } else {
            uuid = UUIDHandler.getUUID(args[0]);
        }
        if (uuid == null) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        if (!plot.denied.contains(uuid)) {
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(plr, C.ALREADY_OWNER);
                return false;
            }
            if (plot.trusted.contains(uuid)) {
                plot.trusted.remove(uuid);
                DBFunc.removeTrusted(loc.getWorld(), plot, uuid);
            }
            if (plot.members.contains(uuid)) {
                plot.members.remove(uuid);
                DBFunc.removeMember(loc.getWorld(), plot, uuid);
            }
            plot.addDenied(uuid);
            DBFunc.setDenied(loc.getWorld(), plot, uuid);
            EventUtil.manager.callDenied(plr, plot, uuid, true);
        } else {
            MainUtil.sendMessage(plr, C.ALREADY_ADDED);
            return false;
        }
        MainUtil.sendMessage(plr, C.DENIED_ADDED);
        if (!uuid.equals(DBFunc.everyone)) {
            PlotSquared.getInstance().IMP.handleKick(uuid, C.YOU_GOT_DENIED);
        }
        return true;
    }
}
