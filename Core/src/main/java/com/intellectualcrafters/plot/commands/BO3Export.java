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

@CommandDeclaration(command = "export",
        aliases = {"save","output"},
        description = "Export a plot",
        permission = "plots.bo3",
        usage = "/plot bo3 export [category] [alias] [-r]",
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE)
public class BO3Export extends SubCommand {

    public BO3Export(Command parent, boolean isStatic) { super(parent, isStatic); }

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

        return BO3Handler.saveBO3(player, plot);
    }
}
