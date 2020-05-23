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
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.query.SortingStrategy;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.uuid.UUIDHandler;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        int page = Integer.MIN_VALUE;
        PlotArea sortByArea = player.getApplicablePlotArea();
        boolean shouldSortByArea = Settings.Teleport.PER_WORLD_VISIT;

        final PlotQuery query = PlotQuery.newQuery();

        switch (args.length) {
            case 3:
                if (!MathMan.isInteger(args[1])) {
                    Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return CompletableFuture.completedFuture(false);
                }
                page = Integer.parseInt(args[2]);
            case 2:
                if (page != Integer.MIN_VALUE || !MathMan.isInteger(args[1])) {
                    sortByArea = PlotSquared.get().getPlotAreaByString(args[1]);
                    if (sortByArea == null) {
                        Captions.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                        Captions.COMMAND_SYNTAX.send(player, getUsage());
                        return CompletableFuture.completedFuture(false);
                    }
                    UUID user = UUIDHandler.getUUIDFromString(args[0]);
                    if (user == null) {
                        Captions.COMMAND_SYNTAX.send(player, getUsage());
                        return CompletableFuture.completedFuture(false);
                    }
                    query.ownedBy(user).whereBasePlot();
                    shouldSortByArea = true;
                    break;
                }
                page = Integer.parseInt(args[1]);
            case 1:
                UUID user = args[0].length() >= 2 ? UUIDHandler.getUUIDFromString(args[0]) : null;
                if (user != null && !PlotSquared.get().hasPlot(user)) {
                    user = null;
                }
                if (page == Integer.MIN_VALUE && user == null && MathMan.isInteger(args[0])) {
                    page = Integer.parseInt(args[0]);
                    query.ownedBy(player).whereBasePlot();
                    break;
                }
                if (user != null) {
                    query.ownedBy(player).whereBasePlot();
                } else {
                    Plot plot = MainUtil.getPlotFromString(player, args[0], true);
                    if (plot != null) {
                        query.withPlot(plot);
                    }
                }
                break;
            case 0:
                page = 1;
                query.ownedBy(player);
                break;
            default:

        }
        if (page == Integer.MIN_VALUE) {
            page = 1;
        }

        // We get the query once,
        // then we get it another time further on
        final List<Plot> unsorted = query.asList();

        if (unsorted.isEmpty()) {
            Captions.FOUND_NO_PLOTS.send(player);
            return CompletableFuture.completedFuture(false);
        }

        if (unsorted.size() > 1) {
            query.whereBasePlot();
        }

        if (page < 1 || page > unsorted.size()) {
            Captions.NOT_VALID_NUMBER.send(player, "(1, " + unsorted.size() + ")");
            return CompletableFuture.completedFuture(false);
        }

        if (shouldSortByArea) {
            query.relativeToArea(sortByArea).withSortingStrategy(SortingStrategy.SORT_BY_CREATION);
        } else {
            query.withSortingStrategy(SortingStrategy.SORT_BY_TEMP);
        }

        final List<Plot> plots = query.asList();

        final Plot plot = plots.get(page - 1);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_UNOWNED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_UNOWNED);
                return CompletableFuture.completedFuture(false);
            }
        } else if (plot.isOwner(player.getUUID())) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OWNED) && !Permissions
                .hasPermission(player, Captions.PERMISSION_HOME)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OWNED);
                return CompletableFuture.completedFuture(false);
            }
        } else if (plot.isAdded(player.getUUID())) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_SHARED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_SHARED);
                return CompletableFuture.completedFuture(false);
            }
        } else {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OTHER)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OTHER);
                return CompletableFuture.completedFuture(false);
            }
            if (!plot.getFlag(UntrustedVisitFlag.class) && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED)) {
                Captions.NO_PERMISSION.send(player, Captions.PERMISSION_ADMIN_VISIT_UNTRUSTED);
                return CompletableFuture.completedFuture(false);
            }
        }

        confirm.run(this, () -> plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {
            if (result) {
                whenDone.run(Visit.this, CommandResult.SUCCESS);
            } else {
                whenDone.run(Visit.this, CommandResult.FAILURE);
            }
        }), () -> whenDone.run(Visit.this, CommandResult.FAILURE));

        return CompletableFuture.completedFuture(true);
    }

}
