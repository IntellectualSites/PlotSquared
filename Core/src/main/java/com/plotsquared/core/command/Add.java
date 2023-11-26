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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "add",
        usage = "/plot add <player | *>",
        category = CommandCategory.SETTINGS,
        permission = "plots.add",
        requiredType = RequiredType.PLAYER)
public class Add extends Command {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Add(final @NonNull EventDispatcher eventDispatcher) {
        super(MainCommand.getInstance(), true);
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public CompletableFuture<Boolean> execute(
            final PlotPlayer<?> player,
            String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), TranslatableCaption.of("errors.not_in_plot"));
        checkTrue(plot.hasOwner(), TranslatableCaption.of("info.plot_unowned"));
        checkTrue(
                plot.isOwner(player.getUUID()) || player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_TRUST),
                TranslatableCaption.of("permission.no_plot_perms")
        );
        checkTrue(
                args.length == 1,
                TranslatableCaption.of("commandconfig.command_syntax"),
                TagResolver.resolver("value", Tag.inserting(Component.text("/plot add <player | *>")))
        );
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        PlayerManager.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof TimeoutException) {
                    player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("errors.invalid_player"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
                    );
                }
                future.completeExceptionally(throwable);
                return;
            } else {
                try {
                    checkTrue(!uuids.isEmpty(), TranslatableCaption.of("errors.invalid_player"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
                    );
                    Iterator<UUID> iterator = uuids.iterator();
                    int size = plot.getTrusted().size() + plot.getMembers().size();
                    while (iterator.hasNext()) {
                        UUID uuid = iterator.next();
                        if (uuid == DBFunc.EVERYONE && !(player.hasPermission(Permission.PERMISSION_TRUST_EVERYONE) || player.hasPermission(
                                Permission.PERMISSION_ADMIN_COMMAND_TRUST))) {
                            player.sendMessage(
                                    TranslatableCaption.of("errors.invalid_player"),
                                    PlotSquared
                                            .platform()
                                            .playerManager()
                                            .getUsernameCaption(uuid)
                                            .thenApply(caption -> TagResolver.resolver(
                                                    "value",
                                                    Tag.inserting(caption.toComponent(player))
                                            ))
                            );
                            iterator.remove();
                            continue;
                        }
                        if (plot.isOwner(uuid)) {
                            player.sendMessage(
                                    TranslatableCaption.of("member.already_added"),
                                    PlotSquared.platform().playerManager().getUsernameCaption(uuid)
                                            .thenApply(caption -> TagResolver.resolver(
                                                    "player",
                                                    Tag.inserting(caption.toComponent(player))
                                            ))
                            );
                            iterator.remove();
                            continue;
                        }
                        if (plot.getMembers().contains(uuid)) {
                            player.sendMessage(
                                    TranslatableCaption.of("member.already_added"),
                                    PlotSquared.platform().playerManager().getUsernameCaption(uuid)
                                            .thenApply(caption -> TagResolver.resolver(
                                                    "player",
                                                    Tag.inserting(caption.toComponent(player))
                                            ))
                            );
                            iterator.remove();
                            continue;
                        }
                        size += plot.getTrusted().contains(uuid) ? 0 : 1;
                    }
                    checkTrue(!uuids.isEmpty(), null);
                    int localAddSize = plot.getMembers().size();
                    int maxAddSize = player.hasPermissionRange(Permission.PERMISSION_ADD, Settings.Limit.MAX_PLOTS);
                    if (localAddSize >= maxAddSize) {
                        player.sendMessage(
                                TranslatableCaption.of("members.plot_max_members_added"),
                                TagResolver.resolver("amount", Tag.inserting(Component.text(localAddSize)))
                        );
                        return;
                    }
                    // Success
                    confirm.run(this, () -> {
                        for (UUID uuid : uuids) {
                            if (uuid != DBFunc.EVERYONE) {
                                if (!plot.removeTrusted(uuid)) {
                                    if (plot.getDenied().contains(uuid)) {
                                        plot.removeDenied(uuid);
                                    }
                                }
                            }
                            plot.addMember(uuid);
                            this.eventDispatcher.callMember(player, plot, uuid, true);
                            player.sendMessage(TranslatableCaption.of("member.member_added"));
                        }
                    }, null);
                } catch (final Throwable exception) {
                    future.completeExceptionally(exception);
                    return;
                }
            }
            future.complete(true);
        });
        return future;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

}
