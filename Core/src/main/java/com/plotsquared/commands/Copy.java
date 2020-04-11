package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.Permissions;

@CommandDeclaration(command = "copy",
    permission = "plots.copy",
    aliases = {"copypaste"},
    category = CommandCategory.CLAIMING,
    description = "Copy a plot",
    usage = "/plot copy <X;Z>",
    requiredType = RequiredType.NONE)
public class Copy extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot1 = location.getPlotAbs();
        if (plot1 == null) {
            return !MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot1.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN.getTranslated())) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (args.length != 1) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        Plot plot2 = MainUtil.getPlotFromString(player, args[0], true);
        if (plot2 == null) {
            return false;
        }
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_ID);
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        if (!plot1.getArea().isCompatible(plot2.getArea())) {
            Captions.PLOTWORLD_INCOMPATIBLE.send(player);
            return false;
        }
        if (plot1.copy(plot2, () -> MainUtil.sendMessage(player, Captions.COPY_SUCCESS))) {
            return true;
        } else {
            MainUtil.sendMessage(player, Captions.REQUIRES_UNOWNED);
            return false;
        }
    }
}
