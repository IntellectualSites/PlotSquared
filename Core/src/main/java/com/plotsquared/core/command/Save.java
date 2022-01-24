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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated In favor of "/plot download" (Arkitektonika) and scheduled
 *         for removal within the next major release.
 */
@Deprecated(forRemoval = true, since = "6.0.9")
@CommandDeclaration(command = "save",
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        permission = "plots.save")
public class Save extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;

    @Inject
    public Save(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull SchematicHandler schematicHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.schematicHandler = schematicHandler;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        final String world = player.getLocation().getWorldName();
        if (!this.plotAreaManager.hasPlotArea(world)) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
            return false;
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
                .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_SAVE)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }
        plot.addRunning();
        this.schematicHandler.getCompoundTag(plot)
                .whenComplete((compoundTag, throwable) -> {
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
                        schematicHandler.upload(compoundTag, uuid, file, new RunnableVal<>() {
                            @Override
                            public void run(URL url) {
                                plot.removeRunning();
                                if (url == null) {
                                    player.sendMessage(TranslatableCaption.of("backups.backup_save_failed"));
                                    return;
                                }
                                player.sendMessage(TranslatableCaption.of("web.save_success"));
                                player.sendMessage(
                                        TranslatableCaption.of("errors.deprecated_commands"),
                                        Template.of("replacement", "/plot download")
                                );
                                try (final MetaDataAccess<List<String>> schematicAccess =
                                             player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_SCHEMATICS)) {
                                    schematicAccess.get().ifPresent(schematics -> schematics.add(file + ".schem"));
                                }
                            }
                        });
                    });
                });
        return true;
    }

}
