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

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@CommandDeclaration(command = "copy",
        permission = "plots.copy",
        aliases = {"copypaste"},
        category = CommandCategory.CLAIMING,
        usage = "/plot copy <X;Z>",
        requiredType = RequiredType.NONE)
public class Copy extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Plot plot1 = player.getCurrentPlot();
        if (plot1 == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot1.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN.toString())) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (args.length != 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("/plot copy <X;Z>")))
            );
            return false;
        }
        Plot plot2 = Plot.getPlotFromString(player, args[0], true);
        if (plot2 == null) {
            return false;
        }
        if (plot1.equals(plot2)) {
            player.sendMessage(TranslatableCaption.of("invalid.origin_cant_be_target"));
            return false;
        }
        if (!plot1.getArea().isCompatible(plot2.getArea())) {
            player.sendMessage(TranslatableCaption.of("errors.plotworld_incompatible"));
            return false;
        }

        plot1.getPlotModificationManager().copy(plot2, player).thenAccept(result -> {
            if (result) {
                player.sendMessage(
                        TranslatableCaption.of("move.copy_success"),
                        TagResolver.builder()
                                .tag("origin", Tag.inserting(Component.text(plot1.toString())))
                                .tag("target", Tag.inserting(Component.text(plot2.toString())))
                                .build()
                );
            } else {
                player.sendMessage(TranslatableCaption.of("move.requires_unowned"));
            }
        });

        return true;
    }

}
