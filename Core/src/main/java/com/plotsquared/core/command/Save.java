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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.jnbt.CompoundTag;
import javax.annotation.Nonnull;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(command = "save",
    description = "Save your plot",
    category = CommandCategory.SCHEMATIC,
    requiredType = RequiredType.NONE,
    permission = "plots.save")
public class Save extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;

    @Inject public Save(@Nonnull final PlotAreaManager plotAreaManager,
                        @Nonnull final SchematicHandler schematicHandler) {
        this.plotAreaManager = plotAreaManager;
        this.schematicHandler = schematicHandler;
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        final String world = player.getLocation().getWorldName();
        if (!this.plotAreaManager.hasPlotArea(world)) {
            return !sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SAVE)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        this.schematicHandler.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
            @Override public void run(final CompoundTag value) {
                TaskManager.runTaskAsync(() -> {
                    String time = (System.currentTimeMillis() / 1000) + "";
                    Location[] corners = plot.getCorners();
                    corners[0] = corners[0].withY(0);
                    corners[1] = corners[1].withY(255);
                    int size = (corners[1].getX() - corners[0].getX()) + 1;
                    PlotId id = plot.getId();
                    String world1 = plot.getArea().toString().replaceAll(";", "-")
                        .replaceAll("[^A-Za-z0-9]", "");
                    final String file = time + '_' + world1 + '_' + id.getX() + '_' + id.getY() + '_' + size;
                    UUID uuid = player.getUUID();
                    schematicHandler.upload(value, uuid, file, new RunnableVal<URL>() {
                        @Override public void run(URL url) {
                            plot.removeRunning();
                            if (url == null) {
                                MainUtil.sendMessage(player, Captions.SAVE_FAILED);
                                return;
                            }
                            MainUtil.sendMessage(player, Captions.SAVE_SUCCESS);
                            try (final MetaDataAccess<List<String>> schematicAccess =
                                player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_SCHEMATICS)) {
                                schematicAccess.get().ifPresent(schematics -> schematics.add(file + ".schem"));
                            }
                        }
                    });
                });
            }
        });
        return true;
    }
}
