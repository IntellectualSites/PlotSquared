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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.query.SortingStrategy;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "home",
        permission = "plots.home",
        usage = "/plot home [<page> | <alias> | <area;x;y> | <area> <x;y> | <area> <page>]",
        aliases = {"h"},
        requiredType = RequiredType.PLAYER,
        category = CommandCategory.TELEPORT)
public class HomeCommand extends Command {

    private final PlotAreaManager plotAreaManager;

    @Inject
    public HomeCommand(final @NonNull PlotAreaManager plotAreaManager) {
        super(MainCommand.getInstance(), true);
        this.plotAreaManager = plotAreaManager;
    }

    private void home(
            final @NonNull PlotPlayer<?> player,
            final @NonNull PlotQuery query, final int page,
            final RunnableVal3<Command, Runnable, Runnable> confirm,
            final RunnableVal2<Command, CommandResult> whenDone
    ) {
        List<Plot> plots = query.asList();
        if (plots.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("invalid.found_no_plots"));
            return;
        } else if (plots.size() < page || page < 1) {
            player.sendMessage(
                    TranslatableCaption.of("invalid.number_not_in_range"),
                    TagResolver.builder()
                            .tag("min", Tag.inserting(Component.text(1)))
                            .tag("max", Tag.inserting(Component.text(plots.size())))
                            .build()
            );
            return;
        }
        Plot plot = plots.get(page - 1);
        confirm.run(this, () -> plot.teleportPlayer(player, TeleportCause.COMMAND_HOME, result -> {
            if (result) {
                whenDone.run(this, CommandResult.SUCCESS);
            } else {
                whenDone.run(HomeCommand.this, CommandResult.FAILURE);
            }
        }), () -> whenDone.run(HomeCommand.this, CommandResult.FAILURE));
    }

    @NonNull
    private PlotQuery query(final @NonNull PlotPlayer<?> player) {
        // everything plots need to have in common here
        return PlotQuery.newQuery().thatPasses(plot -> plot.isOwner(player.getUUID()));
    }

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        // /plot home <number> (or page, whatever it's called)
        // /plot home <alias>
        // /plot home <[area;]x;y>
        // /plot home <area> <x;y>
        // /plot home <area> <page>
        if (!player.hasPermission(Permission.PERMISSION_VISIT_OWNED) && !player.hasPermission(Permission.PERMISSION_HOME)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Component.text(Permission.PERMISSION_VISIT_OWNED.toString())))
            );
            return CompletableFuture.completedFuture(false);
        }
        if (args.length > 2) {
            sendUsage(player);
            return CompletableFuture.completedFuture(false);
        }
        PlotQuery query = query(player);
        int page = 1; // page = index + 1
        String identifier;
        PlotArea plotArea;
        boolean basePlotOnly = true;
        switch (args.length) {
            case 1 -> {
                identifier = args[0];
                if (MathMan.isInteger(identifier)) {
                    try {
                        page = Integer.parseInt(identifier);
                    } catch (NumberFormatException ignored) {
                        player.sendMessage(
                                TranslatableCaption.of("invalid.not_a_number"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(identifier)))
                        );
                        return CompletableFuture.completedFuture(false);
                    }
                    sortBySettings(query, player);
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
                // allow for plot home within a plot area
                plotArea = this.plotAreaManager.getPlotAreaByString(args[0]);
                if (plotArea != null) {
                    query.inArea(plotArea);
                    break;
                }
                // it wasn't a valid plot id, trying to find plot by alias
                query.withAlias(identifier);
            }
            case 2 -> {
                // we assume args[0] is a plot area and args[1] an identifier
                plotArea = this.plotAreaManager.getPlotAreaByString(args[0]);
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
                        player.sendMessage(
                                TranslatableCaption.of("invalid.not_a_number"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(identifier)))
                        );
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
            }
            case 0 -> sortBySettings(query, player);
        }
        if (basePlotOnly) {
            query.whereBasePlot();
        }
        home(player, query, page, confirm, whenDone);
        return CompletableFuture.completedFuture(true);
    }

    private void sortBySettings(PlotQuery plotQuery, PlotPlayer<?> player) {
        // Player may not be in a plot world when attempting to get to a plot home
        PlotArea area = player.getApplicablePlotArea();
        if (Settings.Teleport.PER_WORLD_VISIT && area != null) {
            plotQuery.relativeToArea(area)
                    .withSortingStrategy(SortingStrategy.SORT_BY_CREATION);
        } else {
            plotQuery.withSortingStrategy(SortingStrategy.SORT_BY_TEMP);
        }
    }

    @Override
    public Collection<Command> tab(PlotPlayer<?> player, String[] args, boolean space) {
        final List<Command> completions = new ArrayList<>();
        switch (args.length - 1) {
            case 0 -> {
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
            }
            case 1 -> completions.addAll(
                    TabCompletions.completeNumbers(args[1], 10, 999));
        }
        return completions;
    }

}
