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
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.expiration.PlotAnalysis;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandDeclaration(command = "debugexec",
        permission = "plots.admin",
        aliases = {"exec", "$"},
        category = CommandCategory.DEBUG)
public class DebugExec extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final HybridUtils hybridUtils;


    @Inject
    public DebugExec(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull HybridUtils hybridUtils
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.hybridUtils = hybridUtils;

    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        List<String> allowedParams = Arrays
                .asList(
                        "analyze",
                        "calibrate-analysis",
                        "start-expire",
                        "stop-expire",
                        "remove-flag",
                        "start-rgar",
                        "stop-rgar"
                );
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            switch (arg) {
                case "analyze" -> {
                    Plot plot = player.getCurrentPlot();
                    if (plot == null) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                        return false;
                    }
                    PlotAnalysis analysis = plot.getComplexity(null);
                    if (analysis != null) {
                        player.sendMessage(
                                TranslatableCaption.of("debugexec.changes_column"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(analysis.changes)))
                        );
                        return true;
                    }
                    player.sendMessage(TranslatableCaption.of("debugexec.starting_task"));
                    this.hybridUtils.analyzePlot(plot, new RunnableVal<>() {
                        @Override
                        public void run(PlotAnalysis value) {
                            player.sendMessage(
                                    TranslatableCaption.of("debugexec.analyze_done"),
                                    TagResolver.resolver("command", Tag.inserting(Component.text("/plot debugexec analyze")))
                            );
                        }
                    });
                    return true;
                }
                case "calibrate-analysis" -> {
                    if (args.length != 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                TagResolver.resolver(
                                        "value",
                                        Tag.inserting(Component.text("/plot debugexec analyze <threshold>"))
                                )
                        );
                        player.sendMessage(TranslatableCaption.of("debugexec.threshold_default"));
                        return false;
                    }
                    double threshold;
                    try {
                        threshold = Integer.parseInt(args[1]) / 100d;
                    } catch (NumberFormatException ignored) {
                        player.sendMessage(
                                TranslatableCaption.of("debugexec.invalid_threshold"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(args[1])))
                        );
                        player.sendMessage(TranslatableCaption.of("debugexec.threshold_default_double"));
                        return false;
                    }
                    PlotAnalysis.calcOptimalModifiers(
                            () -> player.sendMessage(TranslatableCaption.of("debugexec.calibration_done")),
                            threshold
                    );
                    return true;
                }
                case "start-expire" -> {
                    if (PlotSquared.platform().expireManager().runAutomatedTask()) {
                        player.sendMessage(TranslatableCaption.of("debugexec.expiry_started"));
                    } else {
                        player.sendMessage(TranslatableCaption.of("debugexec.expiry_already_started"));
                    }
                    return true;
                }
                case "stop-expire" -> {
                    if (!PlotSquared.platform().expireManager().cancelTask()) {
                        player.sendMessage(TranslatableCaption.of("debugexec.task_halted"));
                    } else {
                        player.sendMessage(TranslatableCaption.of("debugexec.task_cancelled"));
                    }
                    return true;
                }
                case "remove-flag" -> {
                    if (args.length != 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                TagResolver.resolver("value", Tag.inserting(Component.text("/plot debugexec remove-flag <flag>")))
                        );
                        return false;
                    }
                    String flag = args[1];
                    final PlotFlag<?, ?> flagInstance =
                            GlobalFlagContainer.getInstance().getFlagFromString(flag);
                    if (flagInstance != null) {
                        for (Plot plot : PlotQuery.newQuery().whereBasePlot()) {
                            PlotFlagRemoveEvent event = this.eventDispatcher
                                    .callFlagRemove(flagInstance, plot);
                            if (event.getEventResult() != Result.DENY) {
                                plot.removeFlag(event.getFlag());
                            }
                        }
                    }
                    player.sendMessage(
                            TranslatableCaption.of("debugexec.cleared_flag"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(flag)))
                    );
                    return true;
                }
                case "start-rgar" -> {
                    if (args.length != 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                TagResolver.resolver(
                                        "value",
                                        Tag.inserting(Component.text("Invalid syntax: /plot debugexec start-rgar <world>"))
                                )
                        );
                        return false;
                    }
                    PlotArea area = this.plotAreaManager.getPlotAreaByString(args[1]);
                    if (area == null) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.not_valid_plot_world"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(args[1])))
                        );
                        return false;
                    }
                    boolean result;
                    if (HybridUtils.regions != null) {
                        result = this.hybridUtils.scheduleRoadUpdate(area, HybridUtils.regions, 0, new LinkedHashSet<>());
                    } else {
                        result = this.hybridUtils.scheduleRoadUpdate(area, 0);
                    }
                    if (!result) {
                        player.sendMessage(TranslatableCaption.of("debugexec.mass_schematic_update_in_progress"));
                        return false;
                    }
                    return true;
                }
                case "stop-rgar" -> {
                    if (!HybridUtils.UPDATE) {
                        player.sendMessage(TranslatableCaption.of("debugexec.task_not_running"));
                        return false;
                    }
                    HybridUtils.UPDATE = false;
                    player.sendMessage(TranslatableCaption.of("debugexec.task_cancelled"));
                    return true;
                }
            }
        }
        player.sendMessage(StaticCaption.of("<prefix><gold>Possible sub commands: </gold><gray>/plot debugexec <"
                + StringMan.join(allowedParams, " | ") + "></gray>"));
        return false;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, String[] args, boolean space) {
        return Stream.of("analyze", "calibrate-analysis", "start-expire", "stop-expire", "remove-flag", "start-rgar", "stop-rgar")
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                .map(value -> new Command(null, false, value, "plots.admin", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
    }

}
