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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.schematic.Schematic;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@CommandDeclaration(command = "schematic",
    permission = "plots.schematic",
    description = "Schematic command",
    aliases = {"sch", "schem"},
    category = CommandCategory.SCHEMATIC,
    usage = "/plot schematic <save|saveall|paste>")
public class SchematicCmd extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final SchematicHandler schematicHandler;
    private boolean running = false;

    @Inject public SchematicCmd(@Nonnull final PlotAreaManager plotAreaManager,
                                @Nonnull final SchematicHandler schematicHandler) {
        this.plotAreaManager = plotAreaManager;
        this.schematicHandler = schematicHandler;
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length < 1) {
            sendMessage(player, Captions.SCHEMATIC_MISSING_ARG);
            return true;
        }
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "paste": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_SCHEMATIC_PASTE)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                        Captions.PERMISSION_SCHEMATIC_PASTE);
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(player, Captions.SCHEMATIC_MISSING_ARG);
                    break;
                }
                Location loc = player.getLocation();
                final Plot plot = loc.getPlotAbs();
                if (plot == null) {
                    return !sendMessage(player, Captions.NOT_IN_PLOT);
                }
                if (!plot.hasOwner()) {
                    MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
                    return false;
                }
                if (!plot.isOwner(player.getUUID()) && !Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SCHEMATIC_PASTE)) {
                    MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(player, Captions.TASK_IN_PROCESS);
                    return false;
                }
                if (plot.isMerged()) {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_PASTE_MERGED);
                    return false;
                }
                final String location = args[1];
                this.running = true;
                TaskManager.runTaskAsync(() -> {
                    Schematic schematic = null;
                    if (location.startsWith("url:")) {
                        try {
                            UUID uuid = UUID.fromString(location.substring(4));
                            URL base = new URL(Settings.Web.URL);
                            URL url = new URL(base, "uploads/" + uuid + ".schematic");
                            schematic = this.schematicHandler.getSchematic(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(player, Captions.SCHEMATIC_INVALID,
                                "non-existent url: " + location);
                            SchematicCmd.this.running = false;
                            return;
                        }
                    } else {
                        try {
                            schematic = this.schematicHandler.getSchematic(location);
                        } catch (SchematicHandler.UnsupportedFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    if (schematic == null) {
                        SchematicCmd.this.running = false;
                        sendMessage(player, Captions.SCHEMATIC_INVALID,
                            "non-existent or not in gzip format");
                        return;
                    }
                    this.schematicHandler.paste(schematic, plot, 0, 1, 0, false, new RunnableVal<Boolean>() {
                            @Override public void run(Boolean value) {
                                SchematicCmd.this.running = false;
                                if (value) {
                                    sendMessage(player, Captions.SCHEMATIC_PASTE_SUCCESS);
                                } else {
                                    sendMessage(player, Captions.SCHEMATIC_PASTE_FAILED);
                                }
                            }
                        });
                });
                break;
            }
            case "saveall":
            case "exportall": {
                if (!(player instanceof ConsolePlayer)) {
                    MainUtil.sendMessage(player, Captions.NOT_CONSOLE);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_WORLD_ARGS);
                    return false;
                }
                PlotArea area = this.plotAreaManager.getPlotAreaByString(args[1]);
                if (area == null) {
                    Captions.NOT_VALID_PLOT_WORLD.send(player, args[1]);
                    return false;
                }
                Collection<Plot> plots = area.getPlots();
                if (plots.isEmpty()) {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_WORLD);
                    return false;
                }
                boolean result = this.schematicHandler.exportAll(plots, null, null,
                    () -> MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_FINISHED));
                if (!result) {
                    MainUtil.sendMessage(player, Captions.TASK_IN_PROCESS);
                    return false;
                } else {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_STARTED);
                    MainUtil.sendMessage(player,
                        "&3Plot&8->&3Schematic&8: &7Found &c" + plots.size() + "&7 plots...");
                }
                break;
            }
            case "export":
            case "save":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_SCHEMATIC_SAVE)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                        Captions.PERMISSION_SCHEMATIC_SAVE);
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(player, Captions.TASK_IN_PROCESS);
                    return false;
                }
                Location location = player.getLocation();
                Plot plot = location.getPlotAbs();
                if (plot == null) {
                    return !sendMessage(player, Captions.NOT_IN_PLOT);
                }
                if (!plot.hasOwner()) {
                    MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
                    return false;
                }
                if (!plot.isOwner(player.getUUID()) && !Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SCHEMATIC_SAVE)) {
                    MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                    return false;
                }
                ArrayList<Plot> plots = Lists.newArrayList(plot);
                boolean result = this.schematicHandler.exportAll(plots, null, null, () -> {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_SINGLE_FINISHED);
                    SchematicCmd.this.running = false;
                });
                if (!result) {
                    MainUtil.sendMessage(player, Captions.TASK_IN_PROCESS);
                    return false;
                } else {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_STARTED);
                }
                break;
            case "list": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_SCHEMATIC_LIST)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                        Captions.PERMISSION_SCHEMATIC_LIST);
                    return false;
                }
                final String string = StringMan.join(this.schematicHandler.getSchematicNames(), "$2, $1");
                Captions.SCHEMATIC_LIST.send(player, string);
            }
            break;
            default:
                sendMessage(player, Captions.SCHEMATIC_MISSING_ARG);
                break;
        }
        return true;
    }
}
