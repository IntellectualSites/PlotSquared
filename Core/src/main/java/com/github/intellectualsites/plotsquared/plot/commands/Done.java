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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.events.PlotDoneEvent;
import com.github.intellectualsites.plotsquared.plot.events.PlotFlagAddEvent;
import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;

@CommandDeclaration(command = "done",
    aliases = {"submit"},
    description = "Mark a plot as done",
    permission = "plots.done",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE)
public class Done extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        PlotDoneEvent event = PlotSquared.get().getEventDispatcher().callDone(plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Done");
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DONE)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (DoneFlag.isDone(plot)) {
            MainUtil.sendMessage(player, Captions.DONE_ALREADY_DONE);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        MainUtil.sendMessage(player, Captions.GENERATING_LINK);
        final Settings.Auto_Clear doneRequirements = Settings.AUTO_CLEAR.get("done");
        if (ExpireManager.IMP == null || doneRequirements == null) {
            finish(plot, player, true);
            plot.removeRunning();
        } else {
            HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
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

    private void finish(Plot plot, PlotPlayer pp, boolean success) {
        if (!success) {
            MainUtil.sendMessage(pp, Captions.DONE_INSUFFICIENT_COMPLEXITY);
            return;
        }
        long flagValue = System.currentTimeMillis() / 1000;
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(DoneFlag.class)
            .createFlagInstance(Long.toString(flagValue));
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(pp, Captions.EVENT_DENIED, "Done flag addition");
            return;
        }
        plot.setFlag(plotFlag);
        MainUtil.sendMessage(pp, Captions.DONE_SUCCESS);
    }
}
