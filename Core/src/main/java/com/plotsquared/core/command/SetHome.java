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

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

@CommandDeclaration(command = "sethome",
    permission = "plots.set.home",
    description = "Set the plot home to your current position",
    usage = "/plot sethome [none]",
    aliases = {"sh", "seth"},
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class SetHome extends SetCommand {

    @Override public boolean set(PlotPlayer player, Plot plot, String value) {
        switch (value.toLowerCase()) {
            case "unset":
            case "reset":
            case "remove":
            case "none": {
                Plot base = plot.getBasePlot(false);
                base.setHome(null);
                return MainUtil.sendMessage(player, Captions.POSITION_UNSET);
            }
            case "":
                Plot base = plot.getBasePlot(false);
                Location bottom = base.getBottomAbs();
                Location location = player.getLocationFull();
                BlockLoc rel = new BlockLoc(location.getX() - bottom.getX(), location.getY(),
                    location.getZ() - bottom.getZ(), location.getYaw(), location.getPitch());
                base.setHome(rel);
                return MainUtil.sendMessage(player, Captions.POSITION_SET);
            default:
                MainUtil.sendMessage(player, Captions.HOME_ARGUMENT);
                return false;
        }
    }
}
