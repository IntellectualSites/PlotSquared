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

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;

@CommandDeclaration(command = "copy",
    permission = "plots.copy",
    aliases = {"copypaste"},
    category = CommandCategory.CLAIMING,
    description = "Copy a plot",
    usage = "/plot copy <X;Z>",
    requiredType = RequiredType.NONE)
public class Copy extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot1 = location.getPlotAbs();
        if (plot1 == null) {
            return !MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot1.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.getTranslated())) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (args.length != 1) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        Plot plot2 = MainUtil.getPlotFromString(player, args[0], true);
        if (plot2 == null) {
            return false;
        }
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_ID);
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        if (!plot1.getArea().isCompatible(plot2.getArea())) {
            Captions.PLOTWORLD_INCOMPATIBLE.send(player);
            return false;
        }
        if (plot1.copy(plot2, () -> MainUtil.sendMessage(player, Captions.COPY_SUCCESS))) {
            return true;
        } else {
            MainUtil.sendMessage(player, Captions.REQUIRES_UNOWNED);
            return false;
        }
    }
}
