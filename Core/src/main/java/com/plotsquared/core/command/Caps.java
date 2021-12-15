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
 *                  Copyright (C) 2021 IntellectualSites
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

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
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
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

import static com.plotsquared.core.util.entity.EntityCategories.CAP_ANIMAL;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_ENTITY;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MISC;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MOB;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_MONSTER;
import static com.plotsquared.core.util.entity.EntityCategories.CAP_VEHICLE;

@CommandDeclaration(command = "caps",
        category = CommandCategory.INFO,
        usage = "/plot caps")
public class Caps extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.miniMessage("errors.not_in_plot"));
            return false;
        }
        if (!plot.isAdded(player.getUUID()) && !Permissions
                .hasPermission(player, Permission.PERMISSION_ADMIN_CAPS_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.miniMessage("permission.no_permission"),
                    Placeholder.miniMessage("node", String.valueOf(Permission.PERMISSION_ADMIN_CAPS_OTHER))
            );
            return false;
        }
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.miniMessage("schematics.schematic_too_large"));
            return false;
        }
        player.sendMessage(TranslatableCaption.miniMessage("info.plot_caps_header"));
        final int[] countedEntities = plot.countEntities();
        sendFormatted(plot, player, MobCapFlag.class, countedEntities, "mobs", CAP_MOB);
        sendFormatted(plot, player, HostileCapFlag.class, countedEntities, "hostile", CAP_MONSTER);
        sendFormatted(plot, player, AnimalCapFlag.class, countedEntities, "animals", CAP_ANIMAL);
        sendFormatted(plot, player, VehicleCapFlag.class, countedEntities, "vehicle", CAP_VEHICLE);
        sendFormatted(plot, player, MiscCapFlag.class, countedEntities, "misc", CAP_MISC);
        sendFormatted(plot, player, EntityCapFlag.class, countedEntities, "entities", CAP_ENTITY);
        return true;
    }

    private <T extends PlotFlag<Integer, T>> void sendFormatted(
            final Plot plot,
            final PlotPlayer<?> player, final Class<T> capFlag, final int[] countedEntities,
            final String name, final int type
    ) {
        final int current = countedEntities[type];
        final int max = plot.getFlag(capFlag);
        final String percentage = String.format("%.1f", 100 * ((float) current / max));
        String maxBeautified = max >= Integer.MAX_VALUE
                ? TranslatableCaption.miniMessage("info.infinite").getComponent(player)
                : String.valueOf(max);
        player.sendMessage(
                TranslatableCaption.miniMessage("info.plot_caps_format"),
                Placeholder.miniMessage("cap", name),
                Placeholder.miniMessage("current", String.valueOf(current)),
                Placeholder.miniMessage("limit", maxBeautified),
                Placeholder.miniMessage("percentage", percentage)
        );
    }

}
