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
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.inject.annotations.WorldFile;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;

@CommandDeclaration(command = "deletearea",
        aliases = "areadelete",
        permission = "plots.area.create",
        usage = "/plot deletearea <area>",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class DeleteArea extends SubCommand {

    private static final PlotId SINGLE_PLOT_ID = PlotId.of(1, 1);

    private final PlotAreaManager plotAreaManager;
    private final YamlConfiguration worldConfiguration;
    private final File worldFile;

    @Inject
    public DeleteArea(
            final @NonNull PlotAreaManager plotAreaManager,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            @WorldFile final @NonNull File worldFile
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldConfiguration = worldConfiguration;
        this.worldFile = worldFile;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        if (args.length != 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(getUsage())))
            );
            return false;
        }

        final PlotArea area = this.plotAreaManager.getPlotAreaByString(args[0]);
        if (area == null) {
            player.sendMessage(
                    TranslatableCaption.of("errors.not_valid_plot_world"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(args[0])))
            );
            return false;
        }

        if (area.getType() != PlotAreaType.PARTIAL
                || !SINGLE_PLOT_ID.equals(area.getMin())
                || !SINGLE_PLOT_ID.equals(area.getMax())) {
            player.sendMessage(StaticCaption.of(
                    "<prefix><red>That area is not a single plot area and cannot be removed with this command.</red>"
            ));
            return false;
        }

        if (area.getPlotCount() != 0) {
            player.sendMessage(StaticCaption.of(
                    "<prefix><red>This single plot area contains claimed plot data. Delete or unclaim the plot before removing the area.</red>"
            ));
            return false;
        }

        final String areaPath = "worlds." + area.getWorldName() + ".areas."
                + area.getId() + '-' + area.getMin() + '-' + area.getMax();
        if (!this.worldConfiguration.contains(areaPath)) {
            player.sendMessage(StaticCaption.of(
                    "<prefix><red>The single plot area exists in memory but its configuration entry could not be found.</red>"
            ));
            return false;
        }

        this.worldConfiguration.set(areaPath, null);
        try {
            this.worldConfiguration.save(this.worldFile);
        } catch (final IOException exception) {
            player.sendMessage(StaticCaption.of(
                    "<prefix><red>Failed to save worlds.yml. The single plot area was not removed from memory.</red>"
            ));
            return false;
        }

        this.plotAreaManager.removePlotArea(area);
        player.sendMessage(StaticCaption.of(
                "<prefix><dark_aqua>Successfully removed single plot area <gold>" + area.getId()
                        + "</gold>. Existing Minecraft terrain was not changed.</dark_aqua>"
        ));
        return true;
    }

}
