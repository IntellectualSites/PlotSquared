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
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

@CommandDeclaration(command = "regenallroads",
        aliases = {"rgar"},
        usage = "/plot regenallroads <world> [height]",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.CONSOLE,
        permission = "plots.regenallroads")
public class RegenAllRoads extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final HybridUtils hybridUtils;

    @Inject
    public RegenAllRoads(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull HybridUtils hybridUtils
    ) {
        this.plotAreaManager = plotAreaManager;
        this.hybridUtils = hybridUtils;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        int height = 0;
        if (args.length == 2) {
            try {
                height = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                player.sendMessage(
                        TranslatableCaption.of("invalid.not_valid_number"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("(0, 256)")))
                );
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("/plot regenallroads <world> [height]")))
                );
                return false;
            }
        } else if (args.length != 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("/plot regenallroads <world> [height]")))
            );
            return false;
        }
        PlotArea area = this.plotAreaManager.getPlotAreaByString(args[0]);
        if (area == null) {
            player.sendMessage(
                    TranslatableCaption.of("errors.not_valid_plot_world"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
            );
            return false;
        }
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            player.sendMessage(TranslatableCaption.of("errors.invalid_plot_world"));
            return false;
        }
        player.sendMessage(
                TranslatableCaption.of("debugroadregen.schematic"),
                TagResolver.resolver("command", Tag.inserting(Component.text("/plot createroadschematic")))
        );
        player.sendMessage(TranslatableCaption.of("debugroadregen.regenallroads_started"));
        boolean result = this.hybridUtils.scheduleRoadUpdate(area, height);
        if (!result) {
            player.sendMessage(TranslatableCaption.of("debugexec.mass_schematic_update_in_progress"));
            return false;
        }
        return true;
    }

}
