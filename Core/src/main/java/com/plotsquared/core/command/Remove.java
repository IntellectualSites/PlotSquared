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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "remove",
    aliases = {"r", "untrust", "ut", "undeny", "unban", "ud"},
    description = "Remove a player from a plot",
    usage = "/plot remove <player|*>",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    permission = "plots.remove")
public class Remove extends SubCommand {

    private final EventDispatcher eventDispatcher;

    @Inject public Remove(@Nonnull final EventDispatcher eventDispatcher) {
        super(Argument.PlayerName);
        this.eventDispatcher = eventDispatcher;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
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

        MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
            int count = 0;
            if (throwable instanceof TimeoutException) {
                MainUtil.sendMessage(player, Captions.FETCHING_PLAYERS_TIMEOUT);
                return;
            } else if (throwable != null) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
                return;
            } else if (!uuids.isEmpty()) {
                for (UUID uuid : uuids) {
                    if (plot.getTrusted().contains(uuid)) {
                        if (plot.removeTrusted(uuid)) {
                            this.eventDispatcher.callTrusted(player, plot, uuid, false);
                            count++;
                        }
                    } else if (plot.getMembers().contains(uuid)) {
                        if (plot.removeMember(uuid)) {
                            this.eventDispatcher.callMember(player, plot, uuid, false);
                            count++;
                        }
                    } else if (plot.getDenied().contains(uuid)) {
                        if (plot.removeDenied(uuid)) {
                            this.eventDispatcher.callDenied(player, plot, uuid, false);
                            count++;
                        }
                    } else if (uuid == DBFunc.EVERYONE) {
                        if (plot.removeTrusted(uuid)) {
                            this.eventDispatcher.callTrusted(player, plot, uuid, false);
                            count++;
                        } else if (plot.removeMember(uuid)) {
                            this.eventDispatcher.callMember(player, plot, uuid, false);
                            count++;
                        } else if (plot.removeDenied(uuid)) {
                            this.eventDispatcher.callDenied(player, plot, uuid, false);
                            count++;
                        }
                    }
                }
            }
            if (count == 0) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            } else {
                MainUtil.sendMessage(player, Captions.REMOVED_PLAYERS, count + "");
            }
        });
        return true;
    }

    @Override public Collection<Command> tab(final PlotPlayer player, final String[] args, final boolean space) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return Collections.emptyList();
        }
        return TabCompletions.completeAddedPlayers(plot, String.join(",", args).trim(),
                Collections.singletonList(player.getName()));
    }

}
