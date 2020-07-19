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
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.query.SortingStrategy;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "home",
                    description = "Teleport to your plot(s)",
                    permission = "plots.home",
                    usage = "/plot home [<page>|<alias>|<area;x;y>|<area> <x;y>|<area> <page>]",
                    aliases = {"h"},
                    requiredType = RequiredType.PLAYER,
                    category = CommandCategory.TELEPORT)
public class HomeCommand extends Command {

    private final PlotAreaManager plotAreaManager;

    @Inject public HomeCommand(@Nonnull final PlotAreaManager plotAreaManager) {
        super(MainCommand.getInstance(), true);
        this.plotAreaManager = plotAreaManager;
    }

    private void home(@Nonnull final PlotPlayer<?> player,
                      @Nonnull final PlotQuery query, final int page,
                      final RunnableVal3<Command, Runnable, Runnable> confirm,
                      final RunnableVal2<Command, CommandResult> whenDone) {
        List<Plot> plots = query.asList();
        if (plots.isEmpty()) {
            Captions.FOUND_NO_PLOTS.send(player);
            return;
        } else if (plots.size() < page) {
            MainUtil.sendMessage(player,
                    String.format(Captions.NUMBER_NOT_IN_RANGE.getTranslated(), "1", plots.size()));
            return;
        }
        Plot plot = plots.get(page - 1);
        confirm.run(this, () -> plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {
            if (result) {
                whenDone.run(this, CommandResult.SUCCESS);
            } else {
                whenDone.run(HomeCommand.this, CommandResult.FAILURE);
            }
        }), () -> whenDone.run(HomeCommand.this, CommandResult.FAILURE));
    }

    @Nonnull private PlotQuery query(@Nonnull final PlotPlayer<?> player) {
        // everything plots need to have in common here
        return PlotQuery.newQuery().ownedBy(player);
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer<?> player, String[] args,
           RunnableVal3<Command, Runnable, Runnable> confirm,
           RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        // /plot home <number> (or page, whatever it's called)
        // /plot home <alias>
        // /plot home <[area;]x;y>
        // /plot home <area> <x;y>
        // /plot home <area> <page>
        if (!Permissions.hasPermission(player, Captions.PERMISSION_VISIT_OWNED) && !Permissions
                .hasPermission(player, Captions.PERMISSION_HOME)) {
            Captions.NO_PERMISSION.send(player, Captions.PERMISSION_VISIT_OWNED);
            return CompletableFuture.completedFuture(false);
        }
        if (args.length > 2) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return CompletableFuture.completedFuture(false);
        }
        PlotQuery query = query(player);
        int page = 1; // page = index + 1
        String identifier;
        boolean basePlotOnly = true;
        switch (args.length) {
            case 1:
                identifier = args[0];
                if (MathMan.isInteger(identifier)) {
                    try {
                        page = Integer.parseInt(identifier);
                    } catch (NumberFormatException ignored) {
                        Captions.NOT_A_NUMBER.send(player, identifier);
                        return CompletableFuture.completedFuture(false);
                    }
                    query.withSortingStrategy(SortingStrategy.SORT_BY_CREATION);
                    break;
                }
                // either plot id or alias
                Plot fromId = Plot.getPlotFromString(player, identifier, false);
                if (fromId != null && fromId.isOwner(player.getUUID())) {
                    // it was a valid plot id
                    basePlotOnly = false;
                    query.withPlot(fromId);
                    break;
                }
                // it wasn't a valid plot id, trying to find plot by alias
                query.withAlias(identifier);
                break;
            case 2:
                // we assume args[0] is a plot area and args[1] an identifier
                final PlotArea plotArea = this.plotAreaManager.getPlotAreaByString(args[0]);
                identifier = args[1];
                if (plotArea == null) {
                    // invalid command, therefore no plots
                    query.noPlots();
                    break;
                }
                query.inArea(plotArea);
                if (MathMan.isInteger(identifier)) {
                    // identifier is a page number
                    try {
                        page = Integer.parseInt(identifier);
                    } catch (NumberFormatException ignored) {
                        Captions.NOT_A_NUMBER.send(player, identifier);
                        return CompletableFuture.completedFuture(false);
                    }
                    query.withSortingStrategy(SortingStrategy.SORT_BY_CREATION);
                    break;
                }
                // identifier needs to be a plot id then
                PlotId id = PlotId.fromStringOrNull(identifier);
                if (id == null) {
                    // invalid command, therefore no plots
                    query.noPlots();
                    break;
                }
                // we can try to get this plot
                Plot plot = plotArea.getPlot(id);
                if (plot == null) {
                    query.noPlots();
                    break;
                }
                // as the query already filters by owner, this is fine
                basePlotOnly = false;
                query.withPlot(plot);
                break;
            case 0:
                query.withSortingStrategy(SortingStrategy.SORT_BY_CREATION);
                break;
        }
        if (basePlotOnly) {
            query.whereBasePlot();
        }
        home(player, query, page, confirm, whenDone);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        final List<Command> completions = new ArrayList<>();
        switch (args.length - 1) {
            case 0:
                completions.addAll(
                        TabCompletions.completeAreas(args[0]));
                if (args[0].isEmpty()) {
                    // if no input is given, only suggest 1 - 3
                    completions.addAll(
                            TabCompletions.asCompletions("1", "2", "3"));
                    break;
                }
                // complete more numbers from the already given input
                completions.addAll(
                        TabCompletions.completeNumbers(args[0], 10, 999));
                break;
            case 1:
                completions.addAll(
                        TabCompletions.completeNumbers(args[1], 10, 999));
                break;
        }
        return completions;
    }
}
