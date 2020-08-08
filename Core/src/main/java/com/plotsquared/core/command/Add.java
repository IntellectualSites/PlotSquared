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
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.uuid.UUIDMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "add",
    description = "Allow a user to build in a plot while the plot owner is online.",
    usage = "/plot add <player|*>",
    category = CommandCategory.SETTINGS,
    permission = "plots.add",
    requiredType = RequiredType.PLAYER)
public class Add extends Command {

    public Add() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
            Captions.NO_PLOT_PERMS);
        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());

        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof TimeoutException) {
                    Captions.FETCHING_PLAYERS_TIMEOUT.send(player);
                } else {
                    Captions.INVALID_PLAYER.send(player, args[0]);
                }
                future.completeExceptionally(throwable);
                return;
            } else {
                try {
                    checkTrue(!uuids.isEmpty(), Captions.INVALID_PLAYER, args[0]);

                    Iterator<UUIDMapping> iterator = uuids.iterator();
                    int size = plot.getTrusted().size() + plot.getMembers().size();
                    while (iterator.hasNext()) {
                        UUIDMapping uuidMapping = iterator.next();
                        if (uuidMapping.getUuid() == DBFunc.EVERYONE && !(
                            Permissions.hasPermission(player, Captions.PERMISSION_TRUST_EVERYONE) || Permissions
                                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST))) {
                            MainUtil.sendMessage(player, Captions.ALREADY_ADDED, uuidMapping.getUsername());
                            iterator.remove();
                            continue;
                        }
                        if (plot.isOwner(uuidMapping.getUuid())) {
                            new Thread(() ->
                                    MainUtil.sendMessage(player, Captions.ALREADY_ADDED, uuidMapping.getUsername())
                            ).start();
                            iterator.remove();
                            continue;
                        }
                        if (plot.getMembers().contains(uuidMapping.getUuid())) {
                            new Thread(() ->
                                    MainUtil.sendMessage(player, Captions.ALREADY_ADDED, uuidMapping.getUsername())
                            ).start();
                            iterator.remove();
                            continue;
                        }
                        size += plot.getTrusted().contains(uuidMapping.getUuid()) ? 0 : 1;
                    }
                    checkTrue(!uuids.isEmpty(), null);
                    checkTrue(size <= plot.getArea().getMaxPlotMembers() || Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
                        Captions.PLOT_MAX_MEMBERS);
                    // Success
                    confirm.run(this, () -> {
                        for (UUIDMapping uuidMapping : uuids) {
                            if (uuidMapping.getUuid() != DBFunc.EVERYONE) {
                                if (!plot.removeTrusted(uuidMapping.getUuid())) {
                                    if (plot.getDenied().contains(uuidMapping.getUuid())) {
                                        plot.removeDenied(uuidMapping.getUuid());
                                    }
                                }
                            }
                            plot.addMember(uuidMapping.getUuid());
                            PlotSquared.get().getEventDispatcher().callMember(player, plot, uuidMapping.getUuid(), true);
                            MainUtil.sendMessage(player, Captions.MEMBER_ADDED);
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
