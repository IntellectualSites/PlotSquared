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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.generator.HybridPlotManager;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import net.kyori.adventure.text.minimessage.Template;
import com.plotsquared.core.queue.QueueCoordinator;

import javax.annotation.Nonnull;
import java.util.Arrays;

@CommandDeclaration(command = "debugroadregen",
    usage = DebugRoadRegen.USAGE,
    requiredType = RequiredType.NONE,
    category = CommandCategory.DEBUG,
    permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {

    public static final String USAGE = "/plot debugroadregen <plot | region [height]>";

    private final HybridUtils hybridUtils;

    @Inject public DebugRoadRegen(@Nonnull final HybridUtils hybridUtils) {
        this.hybridUtils = hybridUtils;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", DebugRoadRegen.USAGE)
            );
            return false;
        }
        String kind = args[0].toLowerCase();
        switch (kind) {
            case "plot":
                return regenPlot(player);
            case "region":
                return regenRegion(player, Arrays.copyOfRange(args, 1, args.length));
            default:
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        Template.of("value", DebugRoadRegen.USAGE)
                );
                return false;
        }
    }

    public boolean regenPlot(PlotPlayer player) {
        Location location = player.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
        } else if (plot.isMerged()) {
            player.sendMessage(TranslatableCaption.of("debug.requires_unmerged"));
        } else {
            PlotManager manager = area.getPlotManager();
            QueueCoordinator queue = area.getQueue();
            manager.createRoadEast(plot, queue);
            manager.createRoadSouth(plot, queue);
            manager.createRoadSouthEast(plot, queue);
            player.sendMessage(
                    TranslatableCaption.of("debugroadregen.regen_done"),
                    Template.of("value", String.valueOf(plot.getId()))
            );
            player.sendMessage(
                    TranslatableCaption.of("debugroadregen.regen_all"),
                    Template.of("value", "/plot regenallroads")
            );
        }
        return true;
    }

    public boolean regenRegion(PlotPlayer player, String[] args) {
        int height = 0;
        if (args.length == 1) {
            try {
                height = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(
                        TranslatableCaption.of("invalid.not_valid_number"),
                        Template.of("value", "0, 256")
                );
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        Template.of("value", DebugRoadRegen.USAGE)
                );
                return false;
            }
        } else if (args.length != 0) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", DebugRoadRegen.USAGE)
            );
            return false;
        }

        Location location = player.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
        }
        Plot plot = player.getCurrentPlot();
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            player.sendMessage(TranslatableCaption.of("errors.invalid_plot_world"));
            return true;
        }
        player.sendMessage(TranslatableCaption.of("debugroadregen.schematic"),
                Template.of("command", "/plot createroadschematic"));
        player.sendMessage(TranslatableCaption.of("debugroadregen.regenallroads"),
                Template.of("command", "/plot regenallroads"));
        boolean result = this.hybridUtils.scheduleSingleRegionRoadUpdate(plot, height);
        if (!result) {
            player.sendMessage(TranslatableCaption.of("debugexec.mass_schematic_update_in_progress"));
            return false;
        }
        return true;
    }
}
