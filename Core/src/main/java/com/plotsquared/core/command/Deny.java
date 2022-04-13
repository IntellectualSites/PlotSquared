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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.world.gamemode.GameModes;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "deny",
        aliases = {"d", "ban"},
        usage = "/plot deny <player>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class Deny extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final WorldUtil worldUtil;

    @Inject
    public Deny(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull WorldUtil worldUtil
    ) {
        super(Argument.PlayerName);
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.worldUtil = worldUtil;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {

        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
                .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_DENY)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return true;
        }

        int maxDenySize = Permissions.hasPermissionRange(player, Permission.PERMISSION_DENY, Settings.Limit.MAX_PLOTS);
        int size = plot.getDenied().size();
        if (size >= maxDenySize) {
            player.sendMessage(
                    TranslatableCaption.of("members.plot_max_members_denied"),
                    Template.of("amount", String.valueOf(size))
            );
            return false;
        }

        PlayerManager.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable instanceof TimeoutException) {
                player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
            } else if (throwable != null || uuids.isEmpty()) {
                player.sendMessage(
                        TranslatableCaption.of("errors.invalid_player"),
                        Template.of("value", args[0])
                );
            } else {
                for (UUID uuid : uuids) {
                    if (uuid == DBFunc.EVERYONE && !(
                            Permissions.hasPermission(player, Permission.PERMISSION_DENY_EVERYONE) || Permissions
                                    .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_DENY))) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.invalid_player"),
                                Template.of("value", args[0])
                        );
                    } else if (plot.isOwner(uuid)) {
                        player.sendMessage(TranslatableCaption.of("deny.cant_remove_owner"));
                        return;
                    } else if (plot.getDenied().contains(uuid)) {
                        player.sendMessage(
                                TranslatableCaption.of("member.already_added"),
                                Template.of("player", PlayerManager.resolveName(uuid).getComponent(player))
                        );
                        return;
                    } else {
                        if (uuid != DBFunc.EVERYONE) {
                            plot.removeMember(uuid);
                            plot.removeTrusted(uuid);
                        }
                        plot.addDenied(uuid);
                        this.eventDispatcher.callDenied(player, plot, uuid, true);
                        if (!uuid.equals(DBFunc.EVERYONE)) {
                            handleKick(PlotSquared.platform().playerManager().getPlayerIfExists(uuid), plot);
                        } else {
                            for (PlotPlayer<?> plotPlayer : plot.getPlayersInPlot()) {
                                // Ignore plot-owners
                                if (plot.isAdded(plotPlayer.getUUID())) {
                                    continue;
                                }
                                handleKick(plotPlayer, plot);
                            }
                        }
                    }
                }
                player.sendMessage(TranslatableCaption.of("deny.denied_added"));
            }
        });

        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

    private void handleKick(PlotPlayer<?> player, Plot plot) {
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
        Location spawn = this.worldUtil.getSpawn(location.getWorldName());
        player.sendMessage(TranslatableCaption.of("deny.you_got_denied"));
        if (plot.equals(spawn.getPlot())) {
            Location newSpawn = this.worldUtil.getSpawn(this.plotAreaManager.getAllWorlds()[0]);
            if (plot.equals(newSpawn.getPlot())) {
                // Kick from server if you can't be teleported to spawn
                // Use string based message here for legacy uses
                player.kick("You got kicked from the plot! This server did not set up a loaded spawn, so you got " +
                        "kicked from the server.");
            } else {
                player.teleport(newSpawn, TeleportCause.DENIED);
            }
        } else {
            player.teleport(spawn, TeleportCause.DENIED);
        }
    }

}
