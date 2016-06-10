package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@CommandDeclaration(
        command = "schematic",
        permission = "plots.schematic",
        description = "Schematic command",
        aliases = {"sch"},
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic <arg...>")
public class SchematicCmd extends SubCommand {

    private boolean running = false;

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 1) {
            sendMessage(player, C.SCHEMATIC_MISSING_ARG);
            return true;
        }
        String arg = args[0].toLowerCase();
        switch (arg) {
            case "paste": {
                if (!Permissions.hasPermission(player, "plots.schematic.paste")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.schematic.paste");
                    return false;
                }
                if (args.length < 2) {
                    sendMessage(player, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                Location loc = player.getLocation();
                final Plot plot = loc.getPlotAbs();
                if (plot == null) {
                    return !sendMessage(player, C.NOT_IN_PLOT);
                }
                if (!plot.hasOwner()) {
                    MainUtil.sendMessage(player, C.PLOT_UNOWNED);
                    return false;
                }
                if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.schematic.paste")) {
                    MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(player, "&cTask is already running.");
                    return false;
                }
                final String location = args[1];
                this.running = true;
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        Schematic schematic;
                        if (location.startsWith("url:")) {
                            try {
                                UUID uuid = UUID.fromString(location.substring(4));
                                URL base = new URL(Settings.Web.URL);
                                URL url = new URL(base, "uploads/" + uuid + ".schematic");
                                schematic = SchematicHandler.manager.getSchematic(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMessage(player, C.SCHEMATIC_INVALID, "non-existent url: " + location);
                                SchematicCmd.this.running = false;
                                return;
                            }
                        } else {
                            schematic = SchematicHandler.manager.getSchematic(location);
                        }
                        if (schematic == null) {
                            SchematicCmd.this.running = false;
                            sendMessage(player, C.SCHEMATIC_INVALID, "non-existent or not in gzip format");
                            return;
                        }
                        SchematicHandler.manager.paste(schematic, plot, 0, 0, 0, true, new RunnableVal<Boolean>() {
                            @Override
                            public void run(Boolean value) {
                                SchematicCmd.this.running = false;
                                if (value) {
                                    sendMessage(player, C.SCHEMATIC_PASTE_SUCCESS);
                                } else {
                                    sendMessage(player, C.SCHEMATIC_PASTE_FAILED);
                                }
                            }
                        });
                    }
                });
                break;
            }
            //            TODO test
            //            case "test": {
            //                if (!Permissions.hasPermission(plr, "plots.schematic.test")) {
            //                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.test");
            //                    return false;
            //                }
            //                if (args.length < 2) {
            //                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
            //                    return false;
            //                }
            //                final Location loc = plr.getLocation();
            //                final Plot plot = MainUtil.getPlot(loc);
            //                if (plot == null) {
            //                    sendMessage(plr, C.NOT_IN_PLOT);
            //                    return false;
            //                }
            //                file = args[1];
            //                schematic = SchematicHandler.manager.getSchematic(file);
            //                if (schematic == null) {
            //                    sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
            //                    return false;
            //                }
            //                final int l1 = schematic.getSchematicDimension().getX();
            //                final int l2 = schematic.getSchematicDimension().getZ();
            //                final int length = MainUtil.getPlotWidth(loc.getWorld(), plot.id);
            //                if ((l1 < length) || (l2 < length)) {
            //                    sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
            //                    break;
            //                }
            //                sendMessage(plr, C.SCHEMATIC_VALID);
            //                break;
            //            }
            case "saveall":
            case "exportall": {
                if (!(player instanceof ConsolePlayer)) {
                    MainUtil.sendMessage(player, C.NOT_CONSOLE);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, "&cNeed world argument. Use &7/plots sch exportall <area>");
                    return false;
                }
                PlotArea area = PS.get().getPlotAreaByString(args[1]);
                if (area == null) {
                    C.NOT_VALID_PLOT_WORLD.send(player, args[1]);
                    return false;
                }
                Collection<Plot> plots = area.getPlots();
                if (plots.isEmpty()) {
                    MainUtil.sendMessage(player, "&cInvalid world. Use &7/plots sch exportall <area>");
                    return false;
                }
                boolean result = SchematicHandler.manager.exportAll(plots, null, null, new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(player, "&aFinished mass export");
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(player, "&cTask is already running.");
                    return false;
                } else {
                    MainUtil.sendMessage(player, "&3PlotSquared&8->&3Schematic&8: &7Mass export has started. This may take a while.");
                    MainUtil.sendMessage(player, "&3PlotSquared&8->&3Schematic&8: &7Found &c" + plots.size() + "&7 plots...");
                }
                break;
            }
            case "export":
            case "save":
                if (!Permissions.hasPermission(player, "plots.schematic.save")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.schematic.save");
                    return false;
                }
                if (this.running) {
                    MainUtil.sendMessage(player, "&cTask is already running.");
                    return false;
                }
                Location location = player.getLocation();
                Plot plot = location.getPlotAbs();
                if (plot == null) {
                    return !sendMessage(player, C.NOT_IN_PLOT);
                }
                if (!plot.hasOwner()) {
                    MainUtil.sendMessage(player, C.PLOT_UNOWNED);
                    return false;
                }
                if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.schematic.save")) {
                    MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
                    return false;
                }
                location.getWorld();
                Collection<Plot> plots = new ArrayList<>();
                plots.add(plot);
                boolean result = SchematicHandler.manager.exportAll(plots, null, null, new Runnable() {
                    @Override
                    public void run() {
                        MainUtil.sendMessage(player, "&aFinished export");
                        SchematicCmd.this.running = false;
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(player, "&cTask is already running.");
                    return false;
                } else {
                    MainUtil.sendMessage(player, "&7Starting export...");
                }
                break;
            default:
                sendMessage(player, C.SCHEMATIC_MISSING_ARG);
                break;
        }
        return true;
    }
}
