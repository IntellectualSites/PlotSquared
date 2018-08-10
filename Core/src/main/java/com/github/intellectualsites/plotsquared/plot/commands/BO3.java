package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.BO3Handler;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

@CommandDeclaration(command = "bo3", aliases = {
    "bo2"}, description = "Mark a plot as done", permission = "plots.bo3", category = CommandCategory.SCHEMATIC, requiredType = RequiredType.NONE)
public class BO3 extends SubCommand {

    public void noArgs(PlotPlayer player) {
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot bo3 export [category] [alias] [-r]");
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot bo3 import <file>");
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null || !plot.hasOwner()) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_BO3)) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length == 0) {
            noArgs(player);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "output":
            case "save":
            case "export":
                return BO3Handler.saveBO3(player, plot);
            case "paste":
            case "load":
            case "import":
            case "input":
                // TODO NOT IMPLEMENTED YET
                MainUtil.sendMessage(player, "NOT IMPLEMENTED YET!!!");
                return false;
            default:
                noArgs(player);
                return false;
        }
    }
}
