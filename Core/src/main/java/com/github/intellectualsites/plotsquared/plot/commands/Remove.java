package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "remove", aliases = {"r", "untrust", "ut", "undeny", "unban", "ud"},
    description = "Remove a player from a plot", usage = "/plot remove <player>",
    category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE,
    permission = "plots.remove") public class Remove extends SubCommand {

    public Remove() {
        super(Argument.PlayerName);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_REMOVE)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return true;
        }
        int count = 0;
        switch (args[0]) {
            case "unknown": {
                HashSet<UUID> all = new HashSet<>();
                all.addAll(plot.getMembers());
                all.addAll(plot.getTrusted());
                all.addAll(plot.getDenied());
                ArrayList<UUID> toRemove = new ArrayList<>();
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
            default:
                Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
                if (!uuids.isEmpty()) {
                    for (UUID uuid : uuids) {
                        if (uuid == DBFunc.EVERYONE) {
                            if (plot.removeTrusted(uuid)) {
                                EventUtil.manager.callTrusted(player, plot, uuid, false);
                                count++;
                            } else if (plot.removeMember(uuid)) {
                                EventUtil.manager.callMember(player, plot, uuid, false);
                                count++;
                            } else if (plot.removeDenied(uuid)) {
                                EventUtil.manager.callDenied(player, plot, uuid, false);
                                count++;
                            }
                        } else if (plot.getTrusted().contains(uuid)) {
                            if (plot.removeTrusted(uuid)) {
                                EventUtil.manager.callTrusted(player, plot, uuid, false);
                                count++;
                            }
                        } else if (plot.getMembers().contains(uuid)) {
                            if (plot.removeMember(uuid)) {
                                EventUtil.manager.callMember(player, plot, uuid, false);
                                count++;
                            }
                        } else if (plot.getDenied().contains(uuid)) {
                            if (plot.removeDenied(uuid)) {
                                EventUtil.manager.callDenied(player, plot, uuid, false);
                                count++;
                            }
                        }
                    }
                }
                break;
        }
        if (count == 0) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        } else {
            MainUtil.sendMessage(player, Captions.REMOVED_PLAYERS, count + "");
        }
        return true;
    }
}
