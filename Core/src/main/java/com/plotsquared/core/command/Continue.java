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
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.EventDispatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

@CommandDeclaration(command = "continue",
        permission = "plots.continue",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class Continue extends SubCommand {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Continue(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if ((plot == null) || !plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_CONTINUE)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(
                            TranslatableCaption.of("permission.no_plot_perms").toComponent(player)
                    ))
            );
            return false;
        }
        if (!DoneFlag.isDone(plot)) {
            player.sendMessage(TranslatableCaption.of("done.done_not_done"));
            return false;
        }
        int size = plot.getConnectedPlots().size();
        int plotCount = Settings.Limit.GLOBAL ? player.getPlotCount() : player.getPlotCount(plot.getWorldName());
        if (!Settings.Done.COUNTS_TOWARDS_LIMIT && (player.getAllowedPlots() < plotCount + size)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.cant_claim_more_plots"),
                    TagResolver.resolver("amount", Tag.inserting(Component.text(player.getAllowedPlots())))
            );
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(DoneFlag.class);
        PlotFlagRemoveEvent event =
                this.eventDispatcher.callFlagRemove(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Done flag removal")))
            );
            return true;
        }
        plot.removeFlag(event.getFlag());
        player.sendMessage(TranslatableCaption.of("done.done_removed"));
        return true;
    }

}
