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
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

@CommandDeclaration(command = "regenallroads",
    aliases = {"rgar"},
    usage = "/plot regenallroads <world> [height]",
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.CONSOLE,
    permission = "plots.regenallroads")
public class RegenAllRoads extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final HybridUtils hybridUtils;

    @Inject public RegenAllRoads(@Nonnull final PlotAreaManager plotAreaManager,
                                 @Nonnull final HybridUtils hybridUtils) {
        this.plotAreaManager = plotAreaManager;
        this.hybridUtils = hybridUtils;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        int height = 0;
        if (args.length == 2) {
            try {
                height = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(
                        TranslatableCaption.of("invalid.not_valid_number"),
                        Template.of("value", "(0, 256)")
                );
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        Template.of("value", "/plot regenallroads <world> [height]")
                );
                return false;
            }
        } else if (args.length != 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot regenallroads <world> [height]")
            );
            return false;
        }
        PlotArea area = this.plotAreaManager.getPlotAreaByString(args[0]);
        if (area == null) {
            player.sendMessage(
                    TranslatableCaption.of("errors.not_valid_plot_world"),
                    Template.of("value", args[0])
            );
            return false;
        }
        String name = args[0];
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            player.sendMessage(TranslatableCaption.of("errors.invalid_plot_world"));
            return false;
        }
        player.sendMessage(TranslatableCaption.of("debugroadregen.schematic"),
                Template.of("command", "/plot createroadschematic"));
        player.sendMessage(TranslatableCaption.of("debugroadregen.regenallroads_started"));
        boolean result = this.hybridUtils.scheduleRoadUpdate(area, height);
        if (!result) {
            player.sendMessage(TranslatableCaption.of("debugexec.mass_schematic_update_in_progress"));
            return false;
        }
        return true;
    }
}
