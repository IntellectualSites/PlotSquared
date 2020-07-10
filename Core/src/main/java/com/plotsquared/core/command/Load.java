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
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import org.jetbrains.annotations.NotNull;

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

    public Load(@NotNull final PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
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
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_LOAD)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }

        if (args.length != 0) {
            if (args.length == 1) {
                List<String> schematics = player.getMeta("plot_schematics");
                if (schematics == null) {
                    // No schematics found:
                    MainUtil.sendMessage(player, Captions.LOAD_NULL);
                    return false;
                }
                String schematic;
                try {
                    schematic = schematics.get(Integer.parseInt(args[0]) - 1);
                } catch (Exception ignored) {
                    // use /plot load <index>
                    MainUtil.sendMessage(player, Captions.NOT_VALID_NUMBER,
                        "(1, " + schematics.size() + ')');
                    return false;
                }
                final URL url;
                try {
                    url = new URL(Settings.Web.URL + "saves/" + player.getUUID() + '/' + schematic);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    MainUtil.sendMessage(player, Captions.LOAD_FAILED);
                    return false;
                }
                plot.addRunning();
                MainUtil.sendMessage(player, Captions.GENERATING_COMPONENT);
                TaskManager.runTaskAsync(() -> {
                    Schematic taskSchematic = SchematicHandler.manager.getSchematic(url);
                    if (taskSchematic == null) {
                        plot.removeRunning();
                        sendMessage(player, Captions.SCHEMATIC_INVALID,
                            "non-existent or not in gzip format");
                        return;
                    }
                    PlotArea area = plot.getArea();
                    SchematicHandler.manager
                        .paste(taskSchematic, plot, 0, area.getMinBuildHeight(), 0, false,
                            new RunnableVal<Boolean>() {
                                @Override public void run(Boolean value) {
                                    plot.removeRunning();
                                    if (value) {
                                        sendMessage(player, Captions.SCHEMATIC_PASTE_SUCCESS);
                                    } else {
                                        sendMessage(player, Captions.SCHEMATIC_PASTE_FAILED);
                                    }
                                }
                            });
                });
                return true;
            }
            plot.removeRunning();
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot load <index>");
            return false;
        }

        // list schematics

        List<String> schematics = player.getMeta("plot_schematics");
        if (schematics == null) {
            plot.addRunning();
            TaskManager.runTaskAsync(() -> {
                List<String> schematics1 = SchematicHandler.manager.getSaves(player.getUUID());
                plot.removeRunning();
                if ((schematics1 == null) || schematics1.isEmpty()) {
                    MainUtil.sendMessage(player, Captions.LOAD_FAILED);
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
                String color = "$4";
                MainUtil.sendMessage(player,
                    "$3[$2" + (i + 1) + "$3] " + color + time + "$3 | " + color + world + ';' + id
                        + "$3 | " + color + size + 'x' + size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        MainUtil.sendMessage(player, Captions.LOAD_LIST);
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
