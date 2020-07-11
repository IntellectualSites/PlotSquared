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
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "kick",
    aliases = "k",
    description = "Kick a player from your plot",
    permission = "plots.kick",
    usage = "/plot kick <player|*>",
    category = CommandCategory.TELEPORT,
    requiredType = RequiredType.PLAYER)
public class Kick extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject public Kick(@NotNull final PlotAreaManager plotAreaManager,
                        @NotNull final WorldUtil worldUtil) {
        super(Argument.PlayerName);
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if ((!plot.hasOwner() || !plot.isOwner(player.getUUID())) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_KICK)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }

        MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable instanceof TimeoutException) {
                MainUtil.sendMessage(player, Captions.FETCHING_PLAYERS_TIMEOUT);
            } else if (throwable != null || uuids.isEmpty()) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            } else {
                Set<PlotPlayer<?>> players = new HashSet<>();
                for (UUID uuid : uuids) {
                    if (uuid == DBFunc.EVERYONE) {
                        for (PlotPlayer<?> pp : plot.getPlayersInPlot()) {
                            if (pp == player || Permissions
                                .hasPermission(pp, Captions.PERMISSION_ADMIN_ENTRY_DENIED)) {
                                continue;
                            }
                            players.add(pp);
                        }
                        continue;
                    }
                    PlotPlayer<?> pp = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                    if (pp != null) {
                        players.add(pp);
                    }
                }
                players.remove(player); // Don't ever kick the calling player
                if (players.isEmpty()) {
                    MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
                    return;
                }
                for (PlotPlayer<?> player2 : players) {
                    if (!plot.equals(player2.getCurrentPlot())) {
                        MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
                        return;
                    }
                    if (Permissions.hasPermission(player2, Captions.PERMISSION_ADMIN_ENTRY_DENIED)) {
                        Captions.CANNOT_KICK_PLAYER.send(player, player2.getName());
                        return;
                    }
                    Location spawn = this.worldUtil.getSpawn(location.getWorldName());
                    Captions.YOU_GOT_KICKED.send(player2);
                    if (plot.equals(spawn.getPlot())) {
                        Location newSpawn = this.worldUtil.getSpawn(this.plotAreaManager.getAllWorlds()[0]);
                        if (plot.equals(newSpawn.getPlot())) {
                            // Kick from server if you can't be teleported to spawn
                            player2.kick(Captions.YOU_GOT_KICKED.getTranslated());
                        } else {
                            player2.plotkick(newSpawn);
                        }
                    } else {
                        player2.plotkick(spawn);
                    }
                }
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
        return TabCompletions.completePlayersInPlot(plot, String.join(",", args).trim(),
                Collections.singletonList(player.getName()));
    }
}
