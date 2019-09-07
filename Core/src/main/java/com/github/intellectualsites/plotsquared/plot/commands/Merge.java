package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.Expression;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.UUID;

@CommandDeclaration(command = "merge", aliases = "m",
    description = "Merge the plot you are standing on with another plot",
    permission = "plots.merge", usage = "/plot merge <all|n|e|s|w> [removeroads]",
    category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, confirmation = true)
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
        Location loc = player.getLocationFull();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        UUID uuid = player.getUUID();
        if (!plot.isOwner(uuid)) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_MERGE)) {
                MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                return false;
            } else {
                uuid = plot.guessOwner();
            }
        }
        final PlotArea plotArea = plot.getArea();
        Expression<Double> priceExr = plotArea.PRICES.getOrDefault("merge", null);
        final int size = plot.getConnectedPlots().size();
        final double price = priceExr == null ? 0d : priceExr.evaluate((double) size);
        if (EconHandler.manager != null && plotArea.USE_ECONOMY && price > 0d
            && EconHandler.manager.getMoney(player) < price) {
            sendMessage(player, Captions.CANNOT_AFFORD_MERGE, String.valueOf(price));
            return false;
        }
        final int maxSize =
            Permissions.hasPermissionRange(player, "plots.merge", Settings.Limit.MAX_PLOTS);
        if (size - 1 > maxSize) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, "plots.merge." + (size + 1));
            return false;
        }
        int direction = -1;
        if (args.length == 0) {
            switch (direction(player.getLocationFull().getYaw())) {
                case "NORTH":
                    direction = 0;
                    break;
                case "EAST":
                    direction = 1;
                    break;
                case "SOUTH":
                    direction = 2;
                    break;
                case "WEST":
                    direction = 3;
                    break;
            }
        } else {
            if ("all".equalsIgnoreCase(args[0]) || "auto".equalsIgnoreCase(args[0])) {
                boolean terrain = true;
                if (args.length == 2) {
                    terrain = "true".equalsIgnoreCase(args[1]);
                }
                if (!terrain && !Permissions
                    .hasPermission(player, Captions.PERMISSION_MERGE_KEEP_ROAD)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                        Captions.PERMISSION_MERGE_KEEP_ROAD.getTranslated());
                    return true;
                }
                if (plot.autoMerge(-1, maxSize, uuid, terrain)) {
                    if (EconHandler.manager != null && plotArea.USE_ECONOMY && price > 0d) {
                        EconHandler.manager.withdrawMoney(player, price);
                        sendMessage(player, Captions.REMOVED_BALANCE, String.valueOf(price));
                    }
                    MainUtil.sendMessage(player, Captions.SUCCESS_MERGE);
                    return true;
                }
                MainUtil.sendMessage(player, Captions.NO_AVAILABLE_AUTOMERGE);
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
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                "/plot merge <" + StringMan.join(values, "|") + "> [removeroads]");
            MainUtil.sendMessage(player,
                Captions.DIRECTION.getTranslated().replaceAll("%dir%", direction(loc.getYaw())));
            return false;
        }
        final boolean terrain;
        if (args.length == 2) {
            terrain = "true".equalsIgnoreCase(args[1]);
        } else {
            terrain = true;
        }
        if (!terrain && !Permissions.hasPermission(player, Captions.PERMISSION_MERGE_KEEP_ROAD)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                Captions.PERMISSION_MERGE_KEEP_ROAD.getTranslated());
            return true;
        }
        if (plot.autoMerge(direction, maxSize - size, uuid, terrain)) {
            if (EconHandler.manager != null && plotArea.USE_ECONOMY && price > 0d) {
                EconHandler.manager.withdrawMoney(player, price);
                sendMessage(player, Captions.REMOVED_BALANCE, String.valueOf(price));
            }
            MainUtil.sendMessage(player, Captions.SUCCESS_MERGE);
            return true;
        }
        Plot adjacent = plot.getRelative(direction);
        if (adjacent == null || !adjacent.hasOwner() || adjacent.getMerged((direction + 2) % 4)
            || adjacent.isOwner(uuid)) {
            MainUtil.sendMessage(player, Captions.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        if (!Permissions.hasPermission(player, Captions.PERMISSION_MERGE_OTHER)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_MERGE_OTHER);
            return false;
        }
        java.util.Set<UUID> uuids = adjacent.getOwners();
        boolean isOnline = false;
        for (final UUID owner : uuids) {
            final PlotPlayer accepter = UUIDHandler.getPlayer(owner);
            if (accepter == null) {
                continue;
            }
            isOnline = true;
            final int dir = direction;
            Runnable run = () -> {
                MainUtil.sendMessage(accepter, Captions.MERGE_ACCEPTED);
                plot.autoMerge(dir, maxSize - size, owner, terrain);
                PlotPlayer plotPlayer = UUIDHandler.getPlayer(player.getUUID());
                if (plotPlayer == null) {
                    sendMessage(accepter, Captions.MERGE_NOT_VALID);
                    return;
                }
                if (EconHandler.manager != null && plotArea.USE_ECONOMY && price > 0d) {
                    if (EconHandler.manager.getMoney(player) < price) {
                        sendMessage(player, Captions.CANNOT_AFFORD_MERGE, String.valueOf(price));
                        return;
                    }
                    EconHandler.manager.withdrawMoney(player, price);
                    sendMessage(player, Captions.REMOVED_BALANCE, String.valueOf(price));
                }
                MainUtil.sendMessage(player, Captions.SUCCESS_MERGE);
            };
            if (hasConfirmation(player)) {
                CmdConfirm.addPending(accepter, Captions.MERGE_REQUEST_CONFIRM.getTranslated()
                    .replaceAll("%s", player.getName()), run);
            } else {
                run.run();
            }
        }
        if (!isOnline) {
            MainUtil.sendMessage(player, Captions.NO_AVAILABLE_AUTOMERGE);
            return false;
        }
        MainUtil.sendMessage(player, Captions.MERGE_REQUESTED);
        return true;
    }
}
