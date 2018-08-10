package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

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
