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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@CommandDeclaration(command = "load",
    aliases = "restore",
    category = CommandCategory.SCHEMATIC,
    requiredType = RequiredType.NONE,
    description = "Load your plot",
    permission = "plots.load",
    usage = "/plot load")
public class Load extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;

    @Inject public Load(@Nonnull final PlotAreaManager plotAreaManager,
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
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_LOAD)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return false;
        }

        if (args.length != 0) {
            if (args.length == 1) {
                List<String> schematics = player.getMeta("plot_schematics");
                if (schematics == null) {
                    // No schematics found:
                    player.sendMessage(TranslatableCaption.of("web.load_null"));
                    return false;
                }
                String schematic;
                try {
                    schematic = schematics.get(Integer.parseInt(args[0]) - 1);
                } catch (Exception ignored) {
                    // use /plot load <index>
                    player.sendMessage(
                            TranslatableCaption.of("invalid.not_valid_number"),
                            Template.of("value", "(1, " + schematics.size() + ')')
                    );
                    return false;
                }
                final URL url;
                try {
                    url = new URL(Settings.Web.URL + "saves/" + player.getUUID() + '/' + schematic);
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
                                Template.of("reason", "non-existent or not in gzip format")
                        );
                        return;
                    }
                    PlotArea area = plot.getArea();
                    this.schematicHandler.paste(taskSchematic, plot, 0, area.getMinBuildHeight(), 0, false,
                            new RunnableVal<Boolean>() {
                                @Override public void run(Boolean value) {
                                    plot.removeRunning();
                                    if (value) {
                                        player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_success"));
                                    } else {
                                        player.sendMessage(TranslatableCaption.of("schematics.schematic_paste_failed"));
                                    }
                                }
                            });
                });
                return true;
            }
            plot.removeRunning();
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot load <index>")
            );
            return false;
        }

        // list schematics

        List<String> schematics = player.getMeta("plot_schematics");
        if (schematics == null) {
            plot.addRunning();
            TaskManager.runTaskAsync(() -> {
                List<String> schematics1 = this.schematicHandler.getSaves(player.getUUID());
                plot.removeRunning();
                if ((schematics1 == null) || schematics1.isEmpty()) {
                    player.sendMessage(TranslatableCaption.of("web.load_failed"));
                    return;
                }
                player.setMeta("plot_schematics", schematics1);
                displaySaves(player);
            });
        } else {
            displaySaves(player);
        }
        return true;
    }

    public void displaySaves(PlotPlayer<?> player) {
        List<String> schematics = player.getMeta("plot_schematics");
        for (int i = 0; i < Math.min(schematics.size(), 32); i++) {
            try {
                String schematic = schematics.get(i).split("\\.")[0];
                String[] split = schematic.split("_");
                if (split.length < 5) {
                    continue;
                }
                String time =
                    secToTime((System.currentTimeMillis() / 1000) - Long.parseLong(split[0]));
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
        player.sendMessage(TranslatableCaption.of("web.load_list"));
    }

    public String secToTime(long time) {
        StringBuilder toreturn = new StringBuilder();
        if (time >= 33868800) {
            int years = (int) (time / 33868800);
            time -= years * 33868800;
            toreturn.append(years).append("y ");
        }
        if (time >= 604800) {
            int weeks = (int) (time / 604800);
            time -= weeks * 604800;
            toreturn.append(weeks).append("w ");
        }
        if (time >= 86400) {
            int days = (int) (time / 86400);
            time -= days * 86400;
            toreturn.append(days).append("d ");
        }
        if (time >= 3600) {
            int hours = (int) (time / 3600);
            time -= hours * 3600;
            toreturn.append(hours).append("h ");
        }
        if (time >= 60) {
            int minutes = (int) (time / 60);
            time -= minutes * 60;
            toreturn.append(minutes).append("m ");
        }
        if (toreturn.length() == 0 || (time > 0)) {
            toreturn.append(time).append("s ");
        }
        return toreturn.toString().trim();
    }
}
