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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
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

    @Inject public Add(@Nonnull final EventDispatcher eventDispatcher) {
        super(MainCommand.getInstance(), true);
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), TranslatableCaption.of("errors.not_in_plot"));
        checkTrue(plot.hasOwner(), TranslatableCaption.of("info.plot_unowned"));
        checkTrue(plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_TRUST),
            TranslatableCaption.of("permission.no_plot_perms"));
        checkTrue(args.length == 1, TranslatableCaption.of("commandconfig.command_syntax"),
                Template.of("value", "/plot add <player | *>"));
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        PlayerManager.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof TimeoutException) {
                    player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                } else {
                    player.sendMessage(TranslatableCaption.of("errors.invalid_player"),
                            Template.of("value", args[0]));
                }
                future.completeExceptionally(throwable);
                return;
            } else {
                try {
                    checkTrue(!uuids.isEmpty(), TranslatableCaption.of("errors.invalid_player"),
                            Template.of("value", args[0]));
                    Iterator<UUID> iterator = uuids.iterator();
                    int size = plot.getTrusted().size() + plot.getMembers().size();
                    while (iterator.hasNext()) {
                        UUID uuid = iterator.next();
                        if (uuid == DBFunc.EVERYONE && !(
                            Permissions.hasPermission(player, Permission.PERMISSION_TRUST_EVERYONE) || Permissions
                                .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_TRUST))) {
                            player.sendMessage(TranslatableCaption.of("errors.invalid_player"),
                                    Template.of("value", PlayerManager.getName(uuid)));
                            iterator.remove();
                            continue;
                        }
                        if (plot.isOwner(uuid)) {
                            player.sendMessage(TranslatableCaption.of("member.already_added"),
                                    Template.of("player", PlayerManager.getName(uuid)));
                            iterator.remove();
                            continue;
                        }
                        if (plot.getMembers().contains(uuid)) {
                            player.sendMessage(TranslatableCaption.of("member.already_added"),
                                    Template.of("player", PlayerManager.getName(uuid)));
                            iterator.remove();
                            continue;
                        }
                        size += plot.getTrusted().contains(uuid) ? 0 : 1;
                    }
                    checkTrue(!uuids.isEmpty(), null);
                    checkTrue(size <= plot.getArea().getMaxPlotMembers() || Permissions.hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_TRUST),
                        TranslatableCaption.of("members.plot_max_members"));
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

    @Override public Collection<Command> tab(final PlotPlayer player, final String[] args, final boolean space) {
        return TabCompletions.completePlayers(String.join(",", args).trim(), Collections.emptyList());
    }

}
