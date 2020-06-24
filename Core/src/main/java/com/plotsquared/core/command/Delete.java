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
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.TaskManager;


@CommandDeclaration(command = "delete",
    permission = "plots.delete",
    description = "Delete the plot you stand on",
    usage = "/plot delete",
    aliases = {"dispose", "del"},
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Delete extends SubCommand {

    // Note: To delete a specific plot use /plot <plot> delete
    // The syntax also works with any command: /plot <plot> <command>

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, Captions.PLOT_UNOWNED);
        }
        Result eventResult =
            PlotSquared.get().getEventDispatcher().callDelete(plot).getEventResult();
        if (eventResult == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Delete");
            return true;
        }
        boolean force = eventResult == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DELETE)) {
            return !sendMessage(player, Captions.NO_PLOT_PERMS);
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        final int currentPlots = Settings.Limit.GLOBAL ?
            player.getPlotCount() :
            player.getPlotCount(location.getWorld());
        Runnable run = () -> {
            if (plot.getRunning() > 0) {
                MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
                return;
            }
            final long start = System.currentTimeMillis();
            boolean result = plot.deletePlot(() -> {
                plot.removeRunning();
                if ((EconHandler.getEconHandler() != null) && plotArea.useEconomy()) {
                    Expression<Double> valueExr = plotArea.getPrices().get("sell");
                    double value = plots.size() * valueExr.evaluate((double) currentPlots);
                    if (value > 0d) {
                        EconHandler.getEconHandler().depositMoney(player, value);
                        sendMessage(player, Captions.ADDED_BALANCE, String.valueOf(value));
                    }
                }
                MainUtil.sendMessage(player, Captions.DELETING_DONE,
                    System.currentTimeMillis() - start);
            });
            if (result) {
                plot.addRunning();
            } else {
                MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, getCommandString() + ' ' + plot.getId(), run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
