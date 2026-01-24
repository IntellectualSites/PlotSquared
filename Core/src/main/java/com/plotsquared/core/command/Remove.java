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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.TabCompletions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "remove",
        aliases = {"r", "untrust", "ut", "undeny", "unban", "ud", "pardon"},
        usage = "/plot remove <player | *>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.remove")
public class Remove extends SubCommand {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Remove(final @NonNull EventDispatcher eventDispatcher) {
        super(Argument.PlayerName);
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_REMOVE)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return true;
        }

        PlayerManager.getUUIDsFromString(args[0], (uuids, throwable) -> {
            int count = 0;
            if (throwable instanceof TimeoutException) {
                player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                return;
            } else if (throwable != null) {
                player.sendMessage(
                        TranslatableCaption.of("errors.invalid_player"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
                );
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
                        count += plot.getTrusted().size();
                        if (plot.removeTrusted(uuid)) {
                            this.eventDispatcher.callTrusted(player, plot, uuid, false);
                        }
                        count += plot.getMembers().size();
                        if (plot.removeMember(uuid)) {
                            this.eventDispatcher.callMember(player, plot, uuid, false);
                        }
                        count += plot.getDenied().size();
                        if (plot.removeDenied(uuid)) {
                            this.eventDispatcher.callDenied(player, plot, uuid, false);
                        }
                    }
                }
            }
            if (count == 0) {
                player.sendMessage(
                        TranslatableCaption.of("member.player_not_removed"),
                        TagResolver.resolver("player", Tag.inserting(Component.text(args[0])))
                );
            } else {
                player.sendMessage(
                        TranslatableCaption.of("member.removed_players"),
                        TagResolver.resolver("amount", Tag.inserting(Component.text(count)))
                );
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
        return TabCompletions.completeAddedPlayers(player, plot, String.join(",", args).trim(),
                Collections.singletonList(player.getName())
        );
    }

}
