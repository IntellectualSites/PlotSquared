package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;
import java.util.UUID;

@CommandDeclaration(
        command = "paste",
        permission = "plots.schematic.paste",
        description = "Paste a schematic from external location in to a plot",
        aliases = {"sch"},
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic paste <location>")
public class SchematicPaste extends SubCommand {

    public SchematicPaste(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
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
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, C.PERMISSION_ADMIN_COMMAND_SCHEMATIC_PASTE)) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }

        final SchematicCmd parent;
        if (getParent() instanceof SchematicCmd) {
            parent = (SchematicCmd) getParent();
        } else {
            MainUtil.sendMessage(player, C.UNKNOWN);
            return false;
        }

        if (parent.getRunning()) {
            MainUtil.sendMessage(player, "&cTask is already running.");
            return false;
        }
        final String location = args[0];
        parent.setRunning(true);
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
                        parent.setRunning(false);
                        return;
                    }
                } else {
                    schematic = SchematicHandler.manager.getSchematic(location);
                }
                if (schematic == null) {
                    parent.setRunning(false);
                    sendMessage(player, C.SCHEMATIC_INVALID, "non-existent or not in gzip format");
                    return;
                }
                SchematicHandler.manager.paste(schematic, plot, 0, 0, 0, true, new RunnableVal<Boolean>() {
                    @Override
                    public void run(Boolean value) {
                        parent.setRunning(false);
                        if (value) {
                            sendMessage(player, C.SCHEMATIC_PASTE_SUCCESS);
                        } else {
                            sendMessage(player, C.SCHEMATIC_PASTE_FAILED);
                        }
                    }
                });
            }
        });
        return true;
    }
}