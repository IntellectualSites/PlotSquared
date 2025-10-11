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
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(usage = "/plot swap <X;Z>",
        command = "swap",
        aliases = {"switch"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.PLAYER)
public class Swap extends SubCommand {

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        Plot plot1 = player.getCurrentPlot();
        if (plot1 == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return CompletableFuture.completedFuture(false);
        }
        if (!plot1.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return CompletableFuture.completedFuture(false);
        }
        if (args.length != 1) {
            sendUsage(player);
            return CompletableFuture.completedFuture(false);
        }
        Plot plot2 = Plot.getPlotFromString(player, args[0], true);
        if (plot2 == null) {
            return CompletableFuture.completedFuture(false);
        }
        if (plot1.equals(plot2)) {
            player.sendMessage(TranslatableCaption.of("invalid.origin_cant_be_target"));
            return CompletableFuture.completedFuture(false);
        }
        if (!plot1.getArea().isCompatible(plot2.getArea())) {
            player.sendMessage(TranslatableCaption.of("errors.plotworld_incompatible"));
            return CompletableFuture.completedFuture(false);
        }
        if (plot1.isMerged() || plot2.isMerged()) {
            player.sendMessage(TranslatableCaption.of("swap.swap_merged"));
            return CompletableFuture.completedFuture(false);
        }

        // Set strings here as the plot objects are mutable (the PlotID changes after being moved).
        String p1 = plot1.toString();
        String p2 = plot2.toString();

        return plot1.getPlotModificationManager().move(
                plot2, player, () -> {
                }, true
        ).thenApply(result -> {
            if (result) {
                player.sendMessage(
                        TranslatableCaption.of("swap.swap_success"),
                        TagResolver.builder()
                                .tag("origin", Tag.inserting(Component.text(p1)))
                                .tag("target", Tag.inserting(Component.text(p2)))
                                .build()
                );
                return true;
            } else {
                player.sendMessage(TranslatableCaption.of("swap.swap_overlap"));
                return false;
            }
        });
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        return true;
    }

}
