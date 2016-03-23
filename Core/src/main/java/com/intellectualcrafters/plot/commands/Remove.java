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
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

@CommandDeclaration(
        command = "remove",
        aliases = {"r"},
        description = "Remove a player from a plot",
        usage = "/plot remove <player>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.remove")
public class Remove extends SubCommand {

    public Remove() {
        this.requiredArguments = new Argument[]{Argument.PlayerName};
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        if (args.length != 1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot remove <player>");
            return true;
        }
        Location loc = plr.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.remove")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        int count = 0;
        switch (args[0]) {
            case "unknown": {
                ArrayList<UUID> toRemove = new ArrayList<>();
                HashSet<UUID> all = new HashSet<>();
                all.addAll(plot.getMembers());
                all.addAll(plot.getTrusted());
                all.addAll(plot.getDenied());
                for (UUID uuid : all) {
                    if (UUIDHandler.getName(uuid) == null) {
                        toRemove.add(uuid);
                        count++;
                    }
                }
                for (UUID uuid : toRemove) {
                    plot.removeDenied(uuid);
                    plot.removeTrusted(uuid);
                    plot.removeMember(uuid);
                }
                break;
            }
            case "*": {
                ArrayList<UUID> toRemove = new ArrayList<>();
                HashSet<UUID> all = new HashSet<>();
                all.addAll(plot.getMembers());
                all.addAll(plot.getTrusted());
                all.addAll(plot.getDenied());
                for (UUID uuid : all) {
                    toRemove.add(uuid);
                    count++;
                }
                for (UUID uuid : toRemove) {
                    plot.removeDenied(uuid);
                    plot.removeTrusted(uuid);
                    plot.removeMember(uuid);
                }
                break;
            }
            default:
                UUID uuid = UUIDHandler.getUUID(args[0], null);
                if (uuid != null) {
                    if (plot.getTrusted().contains(uuid)) {
                        if (plot.removeTrusted(uuid)) {
                            count++;
                        }
                    } else if (plot.getMembers().contains(uuid)) {
                        if (plot.removeMember(uuid)) {
                            count++;
                        }
                    } else if (plot.getDenied().contains(uuid)) {
                        if (plot.removeDenied(uuid)) {
                            count++;
                        }
                    }
                }
                break;
        }
        if (count == 0) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        } else {
            MainUtil.sendMessage(plr, C.REMOVED_PLAYERS, count + "");
        }
        return true;
    }
}
