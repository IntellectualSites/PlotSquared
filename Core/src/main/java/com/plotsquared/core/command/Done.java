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
import com.plotsquared.core.events.PlotDoneEvent;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.expiration.PlotAnalysis;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.RunnableVal;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

@CommandDeclaration(command = "done",
    aliases = {"submit"},
    permission = "plots.done",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE)
public class Done extends SubCommand {

    private final EventDispatcher eventDispatcher;
    private final HybridUtils hybridUtils;

    @Inject public Done(@Nonnull final EventDispatcher eventDispatcher,
                        @Nonnull final HybridUtils hybridUtils) {
        this.eventDispatcher = eventDispatcher;
        this.hybridUtils = hybridUtils;
    }
    
    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        PlotDoneEvent event = this.eventDispatcher.callDone(plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Done"));
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_DONE)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (DoneFlag.isDone(plot)) {
            player.sendMessage(TranslatableCaption.of("done.done_already_done"));
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }
        plot.addRunning();
        player.sendMessage(TranslatableCaption.of("web.generating_link"));
        final Settings.Auto_Clear doneRequirements = Settings.AUTO_CLEAR.get("done");
        if (ExpireManager.IMP == null || doneRequirements == null) {
            finish(plot, player, true);
            plot.removeRunning();
        } else {
            this.hybridUtils.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                @Override public void run(PlotAnalysis value) {
                    plot.removeRunning();
                    boolean result =
                        value.getComplexity(doneRequirements) <= doneRequirements.THRESHOLD;
                    finish(plot, player, result);
                }
            });
        }
        return true;
    }

    private void finish(Plot plot, PlotPlayer player, boolean success) {
        if (!success) {
            player.sendMessage(TranslatableCaption.of("done.done_insufficient_complexity"));
            return;
        }
        long flagValue = System.currentTimeMillis() / 1000;
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(DoneFlag.class)
            .createFlagInstance(Long.toString(flagValue));
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(TranslatableCaption.of("events.event_denied"));
            return;
        }
        plot.setFlag(plotFlag);
        player.sendMessage(TranslatableCaption.of("done.done_success"));
    }
}
