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
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


@CommandDeclaration(command = "delete",
    permission = "plots.delete",
    usage = "/plot delete",
    aliases = {"dispose", "del"},
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Delete extends SubCommand {

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject public Delete(@Nonnull final EventDispatcher eventDispatcher,
                          @Nonnull final EconHandler econHandler) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }
    
    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        Result eventResult = this.eventDispatcher.callDelete(plot).getEventResult();
        if (eventResult == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Delete"));
            return true;
        }
        boolean force = eventResult == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_DELETE)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        final int currentPlots = Settings.Limit.GLOBAL ?
            player.getPlotCount() :
            player.getPlotCount(location.getWorldName());
        Runnable run = () -> {
            if (plot.getRunning() > 0) {
                player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
                return;
            }
            final long start = System.currentTimeMillis();
            boolean result = plot.getPlotModificationManager().deletePlot(() -> {
                plot.removeRunning();
                if (this.econHandler.isEnabled(plotArea)) {
                    Expression<Double> valueExr = plotArea.getPrices().get("sell");
                    double value = plots.size() * valueExr.evaluate((double) currentPlots);
                    if (value > 0d) {
                        this.econHandler.depositMoney(player, value);
                        player.sendMessage(
                                TranslatableCaption.of("economy.added_balance"),
                                Template.of("money", String.valueOf(value))
                        );
                    }
                }
                player.sendMessage(
                        TranslatableCaption.of("working.deleting_done"),
                        Template.of("amount", String.valueOf(System.currentTimeMillis() - start))
                );
            });
            if (result) {
                plot.addRunning();
            } else {
                player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
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
