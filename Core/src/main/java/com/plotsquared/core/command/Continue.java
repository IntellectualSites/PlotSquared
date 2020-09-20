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
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

@CommandDeclaration(command = "continue",
    permission = "plots.continue",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Continue extends SubCommand {

    private final EventDispatcher eventDispatcher;
    
    @Inject public Continue(@Nonnull final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if ((plot == null) || !plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_CONTINUE)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", TranslatableCaption.of("permission.no_plot_perms").getComponent(player))
            );
            return false;
        }
        if (!DoneFlag.isDone(plot)) {
            player.sendMessage(TranslatableCaption.of("done.done_not_done"));
            return false;
        }
        int size = plot.getConnectedPlots().size();
        if (Settings.Done.COUNTS_TOWARDS_LIMIT && (player.getAllowedPlots()
            < player.getPlotCount() + size)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", Permission.PERMISSION_ADMIN_COMMAND_CONTINUE.toString())
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
                    Template.of("value", "Done flag removal"));
            return true;
        }
        plot.removeFlag(event.getFlag());
        player.sendMessage(TranslatableCaption.of("done.done_removed"));
        return true;
    }
}
