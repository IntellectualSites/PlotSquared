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
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.uuid.UUIDMapping;
import com.sk89q.worldedit.world.gamemode.GameModes;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "deny",
    aliases = {"d", "ban"},
    description = "Deny a user from entering a plot",
    usage = "/plot deny <player|*>",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Deny extends SubCommand {

    public Deny() {
        super(Argument.PlayerName);
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {

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
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DENY)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return true;
        }

        MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable instanceof TimeoutException) {
                MainUtil.sendMessage(player, Captions.FETCHING_PLAYERS_TIMEOUT);
            } else if (throwable != null || uuids.isEmpty()) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            } else {
                for (UUIDMapping uuidMapping : uuids) {
                    if (uuidMapping.getUuid() == DBFunc.EVERYONE && !(
                        Permissions.hasPermission(player, Captions.PERMISSION_DENY_EVERYONE) || Permissions
                            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DENY))) {
                        MainUtil.sendMessage(player, Captions.INVALID_PLAYER, uuidMapping.getUsername());
                    } else if (plot.isOwner(uuidMapping.getUuid())) {
                        MainUtil.sendMessage(player, Captions.CANT_REMOVE_OWNER, uuidMapping.getUsername());
                        return;
                    } else if (plot.getDenied().contains(uuidMapping.getUuid())) {
                        MainUtil.sendMessage(player, Captions.ALREADY_ADDED, uuidMapping.getUsername());
                        return;
                    } else {
                        if (uuidMapping.getUuid() != DBFunc.EVERYONE) {
                            plot.removeMember(uuidMapping.getUuid());
                            plot.removeTrusted(uuidMapping.getUuid());
                        }
                        plot.addDenied(uuidMapping.getUuid());
                        PlotSquared.get().getEventDispatcher().callDenied(player, plot, uuidMapping.getUuid(), true);
                        if (!uuidMapping.equals(DBFunc.EVERYONE)) {
                            handleKick(PlotSquared.imp().getPlayerManager().getPlayerIfExists(uuidMapping.getUuid()), plot);
                        } else {
                            for (PlotPlayer plotPlayer : plot.getPlayersInPlot()) {
                                // Ignore plot-owners
                                if (plot.isAdded(plotPlayer.getUUID())) {
                                    continue;
                                }
                                handleKick(plotPlayer, plot);
                            }
                        }
                    }
                }
                MainUtil.sendMessage(player, Captions.DENIED_ADDED);
            }
        });

        return true;
    }

    @Override public Collection<Command> tab(final PlotPlayer player, final String[] args, final boolean space) {
        return TabCompletions.completePlayers(String.join(",", args).trim(), Collections.emptyList());
    }

    private void handleKick(PlotPlayer player, Plot plot) {
        if (player == null) {
            return;
        }
        if (!plot.equals(player.getCurrentPlot())) {
            return;
        }
        if (player.hasPermission("plots.admin.entry.denied")) {
            return;
        }
        if (player.getGameMode() == GameModes.SPECTATOR) {
            player.stopSpectating();
        }
        Location location = player.getLocation();
        Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
        MainUtil.sendMessage(player, Captions.YOU_GOT_DENIED);
        if (plot.equals(spawn.getPlot())) {
            Location newSpawn =
                WorldUtil.IMP.getSpawn(PlotSquared.get().getPlotAreaManager().getAllWorlds()[0]);
            if (plot.equals(newSpawn.getPlot())) {
                // Kick from server if you can't be teleported to spawn
                player.kick(Captions.YOU_GOT_DENIED.getTranslated());
            } else {
                player.teleport(newSpawn);
            }
        } else {
            player.teleport(spawn);
        }
    }
}
