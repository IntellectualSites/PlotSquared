/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.uuid.UUIDHandler;

import java.util.UUID;

@CommandDeclaration(command = "merge",
    aliases = "m",
    description = "Merge the plot you are standing on with another plot",
    permission = "plots.merge",
    usage = "/plot merge <all|n|e|s|w> [removeroads]",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Merge extends SubCommand {

    public static final String[] values = new String[] {"north", "east", "south", "west", "auto"};
    public static final String[] aliases = new String[] {"n", "e", "s", "w", "all"};

    public static String direction(float yaw) {
        yaw = yaw / 90;
        int i = Math.round(yaw);
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

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocationFull();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        Direction direction = null;
        if (args.length == 0) {
            switch (direction(player.getLocationFull().getYaw())) {
                case "NORTH":
                    direction = Direction.NORTH;
                    break;
                case "EAST":
                    direction = Direction.EAST;
                    break;
                case "SOUTH":
                    direction = Direction.SOUTH;
                    break;
                case "WEST":
                    direction = Direction.WEST;
                    break;
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                if (args[0].equalsIgnoreCase(values[i]) || args[0].equalsIgnoreCase(aliases[i])) {
                    direction = Direction.getFromIndex(i);
                    break;
                }
            }
        }
        if (direction == null) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                "/plot merge <" + StringMan.join(values, "|") + "> [removeroads]");
            MainUtil.sendMessage(player, Captions.DIRECTION.getTranslated()
                .replaceAll("%dir%", direction(location.getYaw())));
            return false;
        }
        final int size = plot.getConnectedPlots().size();
        int max = Permissions.hasPermissionRange(player, "plots.merge", Settings.Limit.MAX_PLOTS);
        PlotMergeEvent event =
            PlotSquared.get().getEventDispatcher().callMerge(plot, direction, max, player);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Merge");
            return false;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        direction = event.getDir();
        final int maxSize = event.getMax();

        if (!force && size - 1 > maxSize) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, "plots.merge." + (size + 1));
            return false;
        }
        final PlotArea plotArea = plot.getArea();
        Expression<Double> priceExr = plotArea.getPrices().getOrDefault("merge", null);
        final double price = priceExr == null ? 0d : priceExr.evaluate((double) size);

        UUID uuid = player.getUUID();
        if (direction == Direction.ALL) {
            boolean terrain = true;
            if (args.length == 2) {
                terrain = "true".equalsIgnoreCase(args[1]);
            }
            if (!force && !terrain && !Permissions
                .hasPermission(player, Captions.PERMISSION_MERGE_KEEP_ROAD)) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                    Captions.PERMISSION_MERGE_KEEP_ROAD.getTranslated());
                return true;
            }
            if (plot.autoMerge(Direction.ALL, maxSize, uuid, terrain)) {
                if (EconHandler.manager != null && plotArea.useEconomy() && price > 0d) {
                    EconHandler.manager.withdrawMoney(player, price);
                    sendMessage(player, Captions.REMOVED_BALANCE, String.valueOf(price));
                }
                MainUtil.sendMessage(player, Captions.SUCCESS_MERGE);
                return true;
            }
            MainUtil.sendMessage(player, Captions.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        if (!force && !plot.isOwner(uuid)) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_MERGE)) {
                MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                return false;
            } else {
                uuid = plot.guessOwner();
            }
        }
        if (!force && EconHandler.manager != null && plotArea.useEconomy() && price > 0d
            && EconHandler.manager.getMoney(player) < price) {
            sendMessage(player, Captions.CANNOT_AFFORD_MERGE, String.valueOf(price));
            return false;
        }
        final boolean terrain;
        if (args.length == 2) {
            terrain = "true".equalsIgnoreCase(args[1]);
        } else {
            terrain = true;
        }
        if (!force && !terrain && !Permissions.hasPermission(player, Captions.PERMISSION_MERGE_KEEP_ROAD)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                Captions.PERMISSION_MERGE_KEEP_ROAD.getTranslated());
            return true;
        }
        if (plot.autoMerge(direction, maxSize - size, uuid, terrain)) {
            if (EconHandler.manager != null && plotArea.useEconomy() && price > 0d) {
                EconHandler.manager.withdrawMoney(player, price);
                sendMessage(player, Captions.REMOVED_BALANCE, String.valueOf(price));
            }
            MainUtil.sendMessage(player, Captions.SUCCESS_MERGE);
            return true;
        }
        Plot adjacent = plot.getRelative(direction);
        if (adjacent == null || !adjacent.hasOwner() || adjacent
            .getMerged((direction.getIndex() + 2) % 4) || (!force && adjacent.isOwner(uuid))) {
            MainUtil.sendMessage(player, Captions.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        if (!force && !Permissions.hasPermission(player, Captions.PERMISSION_MERGE_OTHER)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_MERGE_OTHER);
            return false;
        }
        java.util.Set<UUID> uuids = adjacent.getOwners();
        boolean isOnline = false;
        for (final UUID owner : uuids) {
            final PlotPlayer accepter = UUIDHandler.getPlayer(owner);
            if (!force && accepter == null) {
                continue;
            }
            isOnline = true;
            final Direction dir = direction;
            Runnable run = () -> {
                MainUtil.sendMessage(accepter, Captions.MERGE_ACCEPTED);
                plot.autoMerge(dir, maxSize - size, owner, terrain);
                PlotPlayer plotPlayer = UUIDHandler.getPlayer(player.getUUID());
                if (plotPlayer == null) {
                    sendMessage(accepter, Captions.MERGE_NOT_VALID);
                    return;
                }
                if (EconHandler.manager != null && plotArea.useEconomy() && price > 0d) {
                    if (!force && EconHandler.manager.getMoney(player) < price) {
                        sendMessage(player, Captions.CANNOT_AFFORD_MERGE, String.valueOf(price));
                        return;
                    }
                    EconHandler.manager.withdrawMoney(player, price);
                    sendMessage(player, Captions.REMOVED_BALANCE, String.valueOf(price));
                }
                MainUtil.sendMessage(player, Captions.SUCCESS_MERGE);
            };
            if (!force && hasConfirmation(player)) {
                CmdConfirm.addPending(accepter, Captions.MERGE_REQUEST_CONFIRM.getTranslated()
                    .replaceAll("%s", player.getName()), run);
            } else {
                run.run();
            }
        }
        if (!force && !isOnline) {
            MainUtil.sendMessage(player, Captions.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        MainUtil.sendMessage(player, Captions.MERGE_REQUESTED);
        return true;
    }
}
