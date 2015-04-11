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
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Denied extends SubCommand {
    public Denied() {
        super(Command.DENIED, "Manage plot helpers", "denied {add|remove} {player}", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 2) {
            MainUtil.sendMessage(plr, C.DENIED_NEED_ARGUMENT);
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
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.denied")) {
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
            if (!plot.denied.contains(uuid)) {
                if (plot.isOwner(uuid)) {
                    MainUtil.sendMessage(plr, C.ALREADY_OWNER);
                    return false;
                }
                if (plot.trusted.contains(uuid)) {
                    plot.trusted.remove(uuid);
                    DBFunc.removeTrusted(loc.getWorld(), plot, uuid);
                }
                if (plot.helpers.contains(uuid)) {
                    plot.helpers.remove(uuid);
                    DBFunc.removeHelper(loc.getWorld(), plot, uuid);
                }
                plot.addDenied(uuid);
                DBFunc.setDenied(loc.getWorld(), plot, uuid);
                EventUtil.manager.callDenied(plr, plot, uuid, true);
            } else {
                MainUtil.sendMessage(plr, C.ALREADY_ADDED);
                return false;
            }
            final PlotPlayer player = UUIDHandler.getPlayer(uuid);
            if (!uuid.equals(DBFunc.everyone) && (player != null) && player.isOnline()) {
                final Plot pl = MainUtil.getPlot(player.getLocation());
                if ((pl != null) && pl.id.equals(plot.id)) {
                    MainUtil.sendMessage(player, C.YOU_BE_DENIED);
                    player.teleport(BlockManager.manager.getSpawn(loc.getWorld()));
                }
            }
            MainUtil.sendMessage(plr, C.DENIED_ADDED);
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args[1].equalsIgnoreCase("*")) {
                final UUID uuid = DBFunc.everyone;
                if (!plot.denied.contains(uuid)) {
                    MainUtil.sendMessage(plr, C.WAS_NOT_ADDED);
                    return true;
                }
                plot.removeDenied(uuid);
                DBFunc.removeDenied(loc.getWorld(), plot, uuid);
                MainUtil.sendMessage(plr, C.DENIED_REMOVED);
                return true;
            }
            final UUID uuid = UUIDHandler.getUUID(args[1]);
            plot.removeDenied(uuid);
            DBFunc.removeDenied(loc.getWorld(), plot, uuid);
            EventUtil.manager.callDenied(plr, plot, uuid, false);
            MainUtil.sendMessage(plr, C.DENIED_REMOVED);
        } else {
            MainUtil.sendMessage(plr, C.DENIED_NEED_ARGUMENT);
            return true;
        }
        return true;
    }
}
