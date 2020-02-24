package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.schematic.Schematic;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.google.common.collect.Lists;

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

    private boolean running = false;

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
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
                            schematic = SchematicHandler.manager.getSchematic(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(player, Captions.SCHEMATIC_INVALID,
                                "non-existent url: " + location);
                            SchematicCmd.this.running = false;
                            return;
                        }
                    } else {
                        try {
                            schematic = SchematicHandler.manager.getSchematic(location);
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
                    SchematicHandler.manager
                        .paste(schematic, plot, 0, 1, 0, false, new RunnableVal<Boolean>() {
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
                PlotArea area = PlotSquared.get().getPlotAreaByString(args[1]);
                if (area == null) {
                    Captions.NOT_VALID_PLOT_WORLD.send(player, args[1]);
                    return false;
                }
                Collection<Plot> plots = area.getPlots();
                if (plots.isEmpty()) {
                    MainUtil.sendMessage(player, Captions.SCHEMATIC_EXPORTALL_WORLD);
                    return false;
                }
                boolean result = SchematicHandler.manager.exportAll(plots, null, null,
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
                boolean result = SchematicHandler.manager.exportAll(plots, null, null, () -> {
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
                final String string =
                    StringMan.join(SchematicHandler.manager.getSchematicNames(), "$2, $1");
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
