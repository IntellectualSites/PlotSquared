package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Collection;

@CommandDeclaration(
        command = "save",
        permission = "plots.schematic.save",
        description = "Save a schematic to an external location in to a plot",
        aliases = {"export"},
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic save <location>")
public class SchematicSave extends SubCommand {

    public SchematicSave(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(player, getUsage());
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
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, C.PERMISSION_ADMIN_COMMAND_SCHEMATIC_SAVE)) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        location.getWorld();
        Collection<Plot> plots = new ArrayList<>();
        plots.add(plot);
        parent.setRunning(true);
        boolean result = SchematicHandler.manager.exportAll(plots, null, null, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(player, "&aFinished export");
                parent.setRunning(false);
            }
        });
        if (!result) {
            MainUtil.sendMessage(player, "&cTask is already running.");
            return false;
        } else {
            MainUtil.sendMessage(player, "&7Starting export...");
        }

        return true;
    }
}