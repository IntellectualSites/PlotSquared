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
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.HashSet;
import java.util.UUID;

@CommandDeclaration(command = "merge",
aliases = "m",
description = "Merge the plot you are standing on, with another plot",
permission = "plots.merge", usage = "/plot merge <all|n|e|s|w> [removeroads]",
category = CommandCategory.SETTINGS,
requiredType = RequiredType.NONE)
public class Merge extends SubCommand {
    public final static String[] values = new String[] { "north", "east", "south", "west", "auto" };
    public final static String[] aliases = new String[] { "n", "e", "s", "w", "all" };

    public static String direction(float yaw) {
        yaw = yaw / 90;
        final int i = Math.round(yaw);
        switch (i) {
            case -4:
            case 0:
            case 4:
                return "SOUTH";
            case -1:
            case 3:
                return "EAST";
            case -2:
            case 2:
                return "NORTH";
            case -3:
            case 1:
                return "WEST";
            default:
                return "";
        }
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final Location loc = plr.getLocationFull();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        UUID uuid = plr.getUUID();
        if (!plot.isOwner(uuid)) {
            if (!Permissions.hasPermission(plr, "plots.admin.command.merge")) {
                MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
                return false;
            }
            else {
                uuid = plot.owner;
            }
        }
        final PlotArea plotworld = plot.getArea();
        final double price = plotworld.PRICES.get("merge");
        if (EconHandler.manager != null && plotworld.USE_ECONOMY && price > 0d && EconHandler.manager.getMoney(plr) < price) {
            sendMessage(plr, C.CANNOT_AFFORD_MERGE, price + "");
            return false;
        }
        final int size = plot.getConnectedPlots().size();
        final int maxSize = Permissions.hasPermissionRange(plr, "plots.merge", Settings.MAX_PLOTS);
        if (size - 1> maxSize) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.merge." + (size + 1));
            return false;
        }
        int direction = -1;
        if (args.length == 0) {
//            switch (direction(plr.getLocationFull().getYaw())) {
//                case "NORTH":
//                    direction = 0;
//                    break;
//                case "EAST":
//                    direction = 1;
//                    break;
//                case "SOUTH":
//                    direction = 2;
//                    break;
//                case "WEST":
//                    direction = 3;
//                    break;
//            }
        } else {
            if ("all".equalsIgnoreCase(args[0]) || "auto".equalsIgnoreCase(args[0])) {
                boolean terrain = Settings.MERGE_REMOVES_ROADS;
                if (args.length == 2) {
                    terrain = "true".equalsIgnoreCase(args[1]);
                }
                if (plot.autoMerge(-1, maxSize, uuid, terrain)) {
                    if (EconHandler.manager != null && plotworld.USE_ECONOMY && price > 0d) {
                        EconHandler.manager.withdrawMoney(plr, price);
                        sendMessage(plr, C.REMOVED_BALANCE, price + "");
                    }
                    MainUtil.sendMessage(plr, C.SUCCESS_MERGE);
                    return true;
                }
                MainUtil.sendMessage(plr, C.NO_AVAILABLE_AUTOMERGE);
                return false;

            }
            for (int i = 0; i < values.length; i++) {
                if (args[0].equalsIgnoreCase(values[i]) || args[0].equalsIgnoreCase(aliases[i])) {
                    direction = i;
                    break;
                }
            }
        }
        if (direction == -1) {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot merge <" + StringMan.join(values, "|") + "> [removeroads]");
            MainUtil.sendMessage(plr, C.DIRECTION.s().replaceAll("%dir%", direction(loc.getYaw())));
            return false;
        }
        final boolean terrain;
        if (args.length == 2) {
            terrain = "true".equalsIgnoreCase(args[1]);
        } else {
            terrain = Settings.MERGE_REMOVES_ROADS;
        }
        if (plot.autoMerge(direction, maxSize - size, uuid, terrain)) {
            if (EconHandler.manager != null && plotworld.USE_ECONOMY && price > 0d) {
                EconHandler.manager.withdrawMoney(plr, price);
                sendMessage(plr, C.REMOVED_BALANCE, price + "");
            }
            MainUtil.sendMessage(plr, C.SUCCESS_MERGE);
            return true;
        }
        Plot adjacent = plot.getRelative(direction);
        if (adjacent == null || !adjacent.hasOwner() || adjacent.getMerged((direction + 2) % 4) || adjacent.isOwner(uuid)) {
            MainUtil.sendMessage(plr, C.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        if (!Permissions.hasPermission(plr, C.PERMISSION_MERGE_OTHER)) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, C.PERMISSION_MERGE_OTHER);
            return false;
        }
        HashSet<UUID> uuids = adjacent.getOwners();
        boolean isOnline = false;
        for (final UUID owner : uuids) {
            final PlotPlayer accepter = UUIDHandler.getPlayer(owner);
            if (accepter == null) {
                continue;
            }
            isOnline = true;
            final int dir = direction;
            CmdConfirm.addPending(accepter, C.MERGE_REQUEST_CONFIRM.s().replaceAll("%s", plr.getName()), new Runnable() {
                @Override
                public void run() {
                    MainUtil.sendMessage(accepter, C.MERGE_ACCEPTED);
                    plot.autoMerge(dir, maxSize - size, owner, terrain);
                    final PlotPlayer pp = UUIDHandler.getPlayer(plr.getUUID());
                    if (pp == null) {
                        sendMessage(accepter, C.MERGE_NOT_VALID);
                        return;
                    }
                    if (EconHandler.manager != null && plotworld.USE_ECONOMY && price > 0d) {
                        if (EconHandler.manager.getMoney(plr) < price) {
                            sendMessage(plr, C.CANNOT_AFFORD_MERGE, price + "");
                            return;
                        }
                        EconHandler.manager.withdrawMoney(plr, price);
                        sendMessage(plr, C.REMOVED_BALANCE, price + "");
                    }
                    MainUtil.sendMessage(plr, C.SUCCESS_MERGE);
                }
            });
        }
        if (!isOnline) {
            MainUtil.sendMessage(plr, C.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        MainUtil.sendMessage(plr, C.MERGE_REQUESTED);
        return true;
    }
}
