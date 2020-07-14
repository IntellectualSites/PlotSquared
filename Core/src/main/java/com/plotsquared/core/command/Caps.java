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
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.AnimalCapFlag;
import com.plotsquared.core.plot.flag.implementations.EntityCapFlag;
import com.plotsquared.core.plot.flag.implementations.HostileCapFlag;
import com.plotsquared.core.plot.flag.implementations.MiscCapFlag;
import com.plotsquared.core.plot.flag.implementations.MobCapFlag;
import com.plotsquared.core.plot.flag.implementations.VehicleCapFlag;
import com.plotsquared.core.util.Permissions;

import static com.plotsquared.core.util.entity.EntityCategories.CAP_ANIMAL;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ENTITY;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MISC;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MOB;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MONSTER;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_VEHICLE;

@CommandDeclaration(command = "caps",
    category = CommandCategory.INFO,
    description = "Show plot entity caps",
    usage = "/plot caps")
public class Caps extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return Captions.NOT_IN_PLOT.send(player);
        }
        if (!plot.isAdded(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_CAPS_OTHER)) {
            return Captions.NO_PERMISSION.send(player, Captions.PERMISSION_ADMIN_CAPS_OTHER);
        }
        Captions.PLOT_CAPS_HEADER.send(player);
        final int[] countedEntities = plot.countEntities();
        sendFormatted(plot, player, MobCapFlag.class, countedEntities, "mobs", CAP_MOB);
        sendFormatted(plot, player, HostileCapFlag.class, countedEntities, "hostile", CAP_MONSTER);
        sendFormatted(plot, player, AnimalCapFlag.class, countedEntities, "animals", CAP_ANIMAL);
        sendFormatted(plot, player, VehicleCapFlag.class, countedEntities, "vehicle", CAP_VEHICLE);
        sendFormatted(plot, player, MiscCapFlag.class, countedEntities, "misc", CAP_MISC);
        sendFormatted(plot, player, EntityCapFlag.class, countedEntities, "entities", CAP_ENTITY);
        return true;
    }

    private <T extends PlotFlag<Integer, T>> void sendFormatted(final Plot plot,
        final PlotPlayer<?> player, final Class<T> capFlag, final int[] countedEntities,
        final String name, final int type) {
        final int current = countedEntities[type];
        final int max = plot.getFlag(capFlag);
        final String percentage = String.format("%.1f", 100 * ((float) current / max));
        player.sendMessage(Captions.PLOT_CAPS_FORMAT.getTranslated().replace("%cap%", name)
            .replace("%current%", Integer.toString(current))
            .replace("%limit%", Integer.toString(max)).replace("%percentage%", percentage));
    }
}
