package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "import",
        aliases = {"paste","load","input"},
        description = "Import a plot",
        permission = "plots.bo3",
        usage = "/plot bo3 import <file>",
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE)
public class BO3Import extends SubCommand{

    public BO3Import(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null || !plot.hasOwner()) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, C.PERMISSION_ADMIN_COMMAND_BO3)) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }

        // TODO NOT IMPLEMENTED YET
        C.COMMAND_NOT_IMPLEMENTED.send(player, "/plot b03 import");
        return false;
    }
}
