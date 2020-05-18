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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.UntrustedVisitFlag;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@CommandDeclaration(command = "visit",
    permission = "plots.visit",
    description = "Visit someones plot",
    usage = "/plot visit [<player>|<alias>|<world>|<id>] [#]",
    aliases = {"v", "tp", "teleport", "goto", "home", "h", "warp"},
    requiredType = RequiredType.PLAYER,
    category = CommandCategory.TELEPORT)
public class Visit extends Command {

    public Visit() {
        super(MainCommand.getInstance(), true);
    }

    @Override public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        return tabOf(player, args, space, getUsage());
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 1 && args[0].contains(":")) {
            args = args[0].split(":");
        }

        final Collection<Plot> unsortedPre = new HashSet<>();
        final PlotArea[] sortByArea = new PlotArea[] {player.getApplicablePlotArea()};
        final AtomicBoolean shouldSortByArea = new AtomicBoolean(Settings.Teleport.PER_WORLD_VISIT);

        final Consumer<Integer> pageConsumer = page -> {
            if (page == Integer.MIN_VALUE) {
                page = 1;
            }
            if (unsortedPre.isEmpty()) {
                Captions.FOUND_NO_PLOTS.send(player);
            } else {
                final Collection<Plot> unsorted = new ArrayList<>(unsortedPre);
                if (unsorted.size() > 1) {
                    unsorted.removeIf(plot -> !plot.isBasePlot());
                }
                if (page < 1 || page > unsorted.size()) {
                    Captions.NOT_VALID_NUMBER.send(player, "(1, " + unsorted.size() + ")");
                } else {
                    List<Plot> plots;
                    if (shouldSortByArea.get()) {
                        plots = PlotSquared.get().sortPlots(unsorted, PlotSquared.SortType.CREATION_DATE, sortByArea[0]);
                    } else {
                        plots = PlotSquared.get().sortPlotsByTemp(unsorted);
                    }
                    final Plot plot = plots.get(page - 1);
                    if (!plot.hasOwner()) {
                        if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_UNOWNED)) {
                            Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_UNOWNED);
                            return;
                        }
                    } else if (plot.isOwner(player.getUUID())) {
                        if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OWNED) && !Permissions.hasPermission(player, Captions.PERMISSION_HOME)) {
                            Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OWNED);
                            return;
                        }
                    } else if (plot.isAdded(player.getUUID())) {
                        if (!Permissions.hasPermission(player, Captions.PERMISSION_SHARED)) {
                            Captions.NO_PERMISSION.send(player, Captions.PERMISSION_SHARED);
                            return;
                        }
                    } else {
                        if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OTHER)) {
                            Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OTHER);
                            return;
                        }
                        if (!plot.getFlag(UntrustedVisitFlag.class) && !Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED)) {
                            Captions.NO_PERMISSION.send(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED);
                            return;
                        }
                    }
                    confirm.run(this,
                        () -> plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {
                            if (result) {
                                whenDone.run(Visit.this, CommandResult.SUCCESS);
                            } else {
                                whenDone.run(Visit.this, CommandResult.FAILURE);
                            }
                        }), () -> whenDone.run(Visit.this, CommandResult.FAILURE));
                }
            }
        };

        final int[] page = new int[]{Integer.MIN_VALUE};

        switch (args.length) {
            case 3:
                if (!MathMan.isInteger(args[1])) {
                    Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return CompletableFuture.completedFuture(false);
                }
                page[0] = Integer.parseInt(args[2]);
            case 2:
                if (page[0] != Integer.MIN_VALUE || !MathMan.isInteger(args[1])) {
                    sortByArea[0] = PlotSquared.get().getPlotAreaByString(args[1]);
                    if (sortByArea[0] == null) {
                        Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                        Captions.COMMAND_SYNTAX.send(player, getUsage());
                        return CompletableFuture.completedFuture(false);
                    }

                    MainUtil.getUUIDsFromString(args[0], (uuids, throwable) -> {
                        if (throwable != null || uuids.size() != 1) {
                            Captions.COMMAND_SYNTAX.send(player, getUsage());
                        } else {
                            unsortedPre.addAll(PlotSquared.get().getBasePlots((UUID) uuids.toArray()[0]));
                            shouldSortByArea.set(true);
                            pageConsumer.accept(page[0]);
                        }
                    });
                    break;
                }
                page[0] = Integer.parseInt(args[1]);
            case 1:
                final String[] finalArgs = args;
                final Consumer<UUID> uuidConsumer = uuid -> {
                    if (page[0] == Integer.MIN_VALUE && uuid == null && MathMan.isInteger(finalArgs[0])) {
                        page[0] = Integer.parseInt(finalArgs[0]);
                        unsortedPre.addAll(PlotSquared.get().getBasePlots(player));
                    } else {
                        if (uuid != null) {
                            unsortedPre.addAll(PlotSquared.get().getBasePlots(uuid));
                        } else {
                            Plot plot = MainUtil.getPlotFromString(player, finalArgs[0], true);
                            if (plot != null) {
                                unsortedPre.addAll(Collections.singletonList(plot.getBasePlot(false)));
                            }
                        }
                    }
                    pageConsumer.accept(page[0]);
                };

                if (args[0].length() >= 2) {
                    PlotSquared.get().getImpromptuUUIDPipeline().getSingle(args[0], (uuid, throwable) -> {
                        if (uuid != null && !PlotSquared.get().hasPlot(uuid)) {
                            uuidConsumer.accept(null);
                        } else {
                            uuidConsumer.accept(uuid);
                        }
                    });
                } else {
                    uuidConsumer.accept(null);
                }
                break;
            case 0:
                unsortedPre.addAll(PlotSquared.get().getPlots(player));
                pageConsumer.accept(1);
                break;
            default:

        }
        return CompletableFuture.completedFuture(true);
    }

}
