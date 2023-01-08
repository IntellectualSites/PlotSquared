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
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "near",
        aliases = "n",
        usage = "/plot near",
        category = CommandCategory.INFO,
        requiredType = RequiredType.PLAYER)
public class Near extends Command {

    public Near() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), TranslatableCaption.of("errors.not_in_plot"));
        player.sendMessage(
                TranslatableCaption.of("near.plot_near"),
                TagResolver.resolver("list", Tag.inserting(Component.text(StringMan.join(plot.getPlayersInPlot(), ", "))))
        );
        return CompletableFuture.completedFuture(true);
    }

    private List<PlotPlayer<?>> getPlayersInPlotVisible(Plot plot, PlotPlayer<?> executor) {
        return plot.getPlayersInPlot().stream().filter(executor::canSee).toList();
    }

}
