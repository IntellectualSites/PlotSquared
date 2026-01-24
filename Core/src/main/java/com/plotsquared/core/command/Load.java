/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.TimeUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@CommandDeclaration(command = "load",
        aliases = "restore",
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        permission = "plots.load",
        usage = "/plot load")
public class Load extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;

    @Inject
    public Load(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull SchematicHandler schematicHandler
    ) {
        this.plotAreaManager = plotAreaManager;
        this.schematicHandler = schematicHandler;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_LOAD)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }

        try (final MetaDataAccess<List<String>> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_SCHEMATICS)) {
            if (args.length != 0) {
                if (args.length == 1) {
                    List<String> schematics = metaDataAccess.get().orElse(null);
                    if (schematics == null) {
                        // No schematics found:
                        player.sendMessage(
                                TranslatableCaption.of("web.load_null"),
                                TagResolver.resolver("command", Tag.inserting(Component.text("/plot load")))
                        );
                        return false;
                    }
                    String schematic;
                    try {
                        schematic = schematics.get(Integer.parseInt(args[0]) - 1);
                    } catch (Exception ignored) {
                        // use /plot load <index>
                        player.sendMessage(
                                TranslatableCaption.of("invalid.not_valid_number"),
                                TagResolver.resolver("value", Tag.inserting(Component.text("(1, " + schematics.size() + ')')))
                        );
                        return false;
                    }
                    final URL url;
                    try {
                        url = URI.create(Settings.Web.URL + "saves/" + player.getUUID() + '/' + schematic).toURL();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        player.sendMessage(TranslatableCaption.of("web.load_failed"));
                        return false;
                    }
                    plot.addRunning();
                    player.sendMessage(TranslatableCaption.of("working.generating_component"));
                    TaskManager.runTaskAsync(() -> {
                        Schematic taskSchematic = this.schematicHandler.getSchematic(url);
                        if (taskSchematic == null) {
                            plot.removeRunning();
                            player.sendMessage(
                                    TranslatableCaption.of("schematics.schematic_invalid"),
                                    TagResolver.resolver(
                                            "reason",
                                            Tag.inserting(Component.text("non-existent or not in gzip format"))
                                    )
                            );
                            return;
                        }
                        PlotArea area = plot.getArea();
                        this.schematicHandler.paste(
                                taskSchematic,
                                plot,
                                0,
                                area.getMinBuildHeight(),
                                0,
                                false,
                                player,
                                new RunnableVal<>() {
                                    @Override
                                    public void run(Boolean value) {
                                        plot.removeRunning();
                                        if (value) {
                                            player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_success"));
                                        } else {
                                            player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_failed"));
                                        }
                                    }
                                }
                        );
                    });
                    return true;
                }
                plot.removeRunning();
                player.sendMessage(
                        TranslatableCaption.of("commandconfig.command_syntax"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("/plot load <index>")))
                );
                return false;
            }

            // list schematics

            List<String> schematics = metaDataAccess.get().orElse(null);
            if (schematics == null) {
                plot.addRunning();
                TaskManager.runTaskAsync(() -> {
                    List<String> schematics1 = this.schematicHandler.getSaves(player.getUUID());
                    plot.removeRunning();
                    if ((schematics1 == null) || schematics1.isEmpty()) {
                        player.sendMessage(TranslatableCaption.of("web.load_failed"));
                        return;
                    }
                    metaDataAccess.set(schematics1);
                    displaySaves(player);
                });
            } else {
                displaySaves(player);
            }
        }
        return true;
    }

    public void displaySaves(PlotPlayer<?> player) {
        try (final MetaDataAccess<List<String>> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_SCHEMATICS)) {
            List<String> schematics = metaDataAccess.get().orElse(Collections.emptyList());
            for (int i = 0; i < Math.min(schematics.size(), 32); i++) {
                try {
                    String schematic = schematics.get(i).split("\\.")[0];
                    String[] split = schematic.split("_");
                    if (split.length < 5) {
                        continue;
                    }
                    String time = TimeUtil.secToTime((System.currentTimeMillis() / 1000) - Long.parseLong(split[0]));
                    String world = split[1];
                    PlotId id = PlotId.fromString(split[2] + ';' + split[3]);
                    String size = split[4];
                    String color = "<dark_aqua>";
                    player.sendMessage(StaticCaption.of("<dark_gray>[</dark_gray><gray>" + (i + 1) + "</gray><dark_aqua>] </dark_aqua>" + color + time + "<dark_gray> | </dark_gray>" + color + world + ';' + id
                            + "<dark_gray> | </dark_gray>" + color + size + 'x' + size));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            player.sendMessage(
                    TranslatableCaption.of("web.load_list"),
                    TagResolver.resolver("command", Tag.inserting(Component.text("/plot load #")))
            );
        }
    }

}
