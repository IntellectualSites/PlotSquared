/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "kick",
        aliases = "k",
        permission = "plots.kick",
        usage = "/plot kick <player | *>",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.PLAYER)
public class Kick extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject
    public Kick(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull WorldUtil worldUtil
    ) {
        super(Argument.PlayerName);
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if ((!plot.hasOwner() || !plot.isOwner(player.getUUID())) && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_KICK)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }

        PlayerManager.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable instanceof TimeoutException) {
                player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
            } else if (throwable != null || uuids.isEmpty()) {
                player.sendMessage(
                        TranslatableCaption.of("errors.invalid_player"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
                );
            } else {
                Set<PlotPlayer<?>> players = new HashSet<>();
                for (UUID uuid : uuids) {
                    if (uuid == DBFunc.EVERYONE) {
                        for (PlotPlayer<?> pp : plot.getPlayersInPlot()) {
                            if (pp == player || pp.hasPermission(Permission.PERMISSION_ADMIN_ENTRY_DENIED)) {
                                continue;
                            }
                            players.add(pp);
                        }
                        continue;
                    }
                    PlotPlayer<?> pp = PlotSquared.platform().playerManager().getPlayerIfExists(uuid);
                    if (pp != null) {
                        players.add(pp);
                    }
                }
                players.remove(player); // Don't ever kick the calling player
                if (players.isEmpty()) {
                    player.sendMessage(
                            TranslatableCaption.of("errors.invalid_player"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
                    );
                    return;
                }
                for (PlotPlayer<?> player2 : players) {
                    if (!plot.equals(player2.getCurrentPlot())) {
                        player.sendMessage(
                                TranslatableCaption.of("kick.player_not_in_plot"),
                                TagResolver.resolver("player", Tag.inserting(Component.text(player2.getName())))
                        );
                        return;
                    }
                    if (player2.hasPermission(Permission.PERMISSION_ADMIN_ENTRY_DENIED)) {
                        player.sendMessage(
                                TranslatableCaption.of("kick.cannot_kick_player"),
                                TagResolver.resolver("player", Tag.inserting(Component.text(player2.getName())))
                        );
                        return;
                    }
                    Location spawn = this.worldUtil.getSpawn(plot.getWorldName());
                    player2.sendMessage(TranslatableCaption.of("kick.you_got_kicked"));
                    if (plot.equals(spawn.getPlot())) {
                        Location newSpawn = this.worldUtil.getSpawn(this.plotAreaManager.getAllWorlds()[0]);
                        if (plot.equals(newSpawn.getPlot())) {
                            // Kick from server if you can't be teleported to spawn
                            // Use string based message here for legacy uses
                            player2.kick("You got kicked from the plot! This server did not set up a loaded spawn, so you got " +
                                    "kicked from the server.");
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

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return Collections.emptyList();
        }
        return TabCompletions.completePlayersInPlot(player, plot, String.join(",", args).trim(),
                Collections.singletonList(player.getName())
        );
    }

}
