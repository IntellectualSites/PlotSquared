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
import com.plotsquared.core.generator.HybridPlotManager;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.queue.QueueCoordinator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandDeclaration(command = "debugroadregen",
        usage = DebugRoadRegen.USAGE,
        requiredType = RequiredType.NONE,
        category = CommandCategory.DEBUG,
        permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {

    public static final String USAGE = "/plot debugroadregen <plot | region [height]>";

    private final HybridUtils hybridUtils;

    @Inject
    public DebugRoadRegen(final @NonNull HybridUtils hybridUtils) {
        this.hybridUtils = hybridUtils;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if (args.length < 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(DebugRoadRegen.USAGE)))
            );
            return false;
        }

        PlotArea area = player.getPlotAreaAbs();
        check(area, TranslatableCaption.of("errors.not_in_plot_world"));
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return false;
        }
        String kind = args[0].toLowerCase();
        switch (kind) {
            case "plot" -> {
                return regenPlot(player);
            }
            case "region" -> {
                return regenRegion(player, Arrays.copyOfRange(args, 1, args.length));
            }
            default -> {
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(DebugRoadRegen.USAGE)))
                );
                return false;
            }
        }
    }

    public boolean regenPlot(PlotPlayer<?> player) {
        PlotArea area = player.getContextualPlotArea();
        if (area == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
            return false;
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
        } else if (plot.isMerged()) {
            player.sendMessage(TranslatableCaption.of("debug.requires_unmerged"));
        } else {
            PlotManager manager = area.getPlotManager();
            QueueCoordinator queue = area.getQueue();
            queue.setCompleteTask(() -> {
                player.sendMessage(
                        TranslatableCaption.of("debugroadregen.regen_done"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(plot.getId().toString())))
                );
                player.sendMessage(
                        TranslatableCaption.of("debugroadregen.regen_all"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("/plot regenallroads")))
                );
            });
            manager.createRoadEast(plot, queue);
            manager.createRoadSouth(plot, queue);
            manager.createRoadSouthEast(plot, queue);
            queue.enqueue();
        }
        return true;
    }

    public boolean regenRegion(PlotPlayer<?> player, String[] args) {
        int height = 0;
        if (args.length == 1) {
            try {
                height = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(
                        TranslatableCaption.of("invalid.not_valid_number"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("0, 256")))
                );
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(DebugRoadRegen.USAGE)))
                );
                return false;
            }
        } else if (args.length != 0) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(DebugRoadRegen.USAGE)))
            );
            return false;
        }

        PlotArea area = player.getContextualPlotArea();
        if (area == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
            return false;
        }
        Plot plot = player.getCurrentPlot();
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            player.sendMessage(TranslatableCaption.of("errors.invalid_plot_world"));
            return true;
        }
        player.sendMessage(
                TranslatableCaption.of("debugroadregen.schematic"),
                TagResolver.resolver("command", Tag.inserting(Component.text("/plot createroadschematic")))
        );
        player.sendMessage(
                TranslatableCaption.of("debugroadregen.regenallroads"),
                TagResolver.resolver("command", Tag.inserting(Component.text("/plot regenallroads")))
        );
        boolean result = this.hybridUtils.scheduleSingleRegionRoadUpdate(plot, height);
        if (!result) {
            player.sendMessage(TranslatableCaption.of("debugexec.mass_schematic_update_in_progress"));
            return false;
        }
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, String[] args, boolean space) {
        return Stream.of("plot", "region")
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                .map(value -> new Command(null, false, value, "plots.debugroadregen", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
    }

}
