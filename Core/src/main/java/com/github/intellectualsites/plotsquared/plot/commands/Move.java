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

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(usage = "/plot move <X;Z>",
    command = "move",
    description = "Move a plot",
    permission = "plots.move",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.PLAYER)
public class Move extends SubCommand {

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        Location location = player.getLocation();
        Plot plot1 = location.getPlotAbs();
        if (plot1 == null) {
            return CompletableFuture
                .completedFuture(!MainUtil.sendMessage(player, Captions.NOT_IN_PLOT));
        }
        if (!plot1.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.getTranslated())) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return CompletableFuture.completedFuture(false);
        }
        boolean override = false;
        if (args.length == 2 && args[1].equalsIgnoreCase("-f")) {
            args = new String[] {args[0]};
            override = true;
        }
        if (args.length != 1) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return CompletableFuture.completedFuture(false);
        }
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        Plot plot2;
        if (area == null) {
            plot2 = MainUtil.getPlotFromString(player, args[0], true);
            if (plot2 == null) {
                return CompletableFuture.completedFuture(false);
            }
        } else {
            plot2 = area.getPlotAbs(plot1.getId());
        }
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_ID);
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot copy <X;Z>");
            return CompletableFuture.completedFuture(false);
        }
        if (!plot1.getArea().isCompatible(plot2.getArea()) && (!override || !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.getTranslated()))) {
            Captions.PLOTWORLD_INCOMPATIBLE.send(player);
            return CompletableFuture.completedFuture(false);
        }
        if (plot1.isMerged() || plot2.isMerged()) {
            Captions.MOVE_MERGED.send(player);
            return CompletableFuture.completedFuture(false);
        }

        return plot1.move(plot2, () -> {}, false)
            .thenApply(result -> {
                if (result) {
                    MainUtil.sendMessage(player, Captions.MOVE_SUCCESS);
                    return true;
                } else {
                    MainUtil.sendMessage(player, Captions.REQUIRES_UNOWNED);
                    return false;
                }
            });
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        return true;
    }

}
