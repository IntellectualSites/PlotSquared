package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "copy",
        permission = "plots.copy",
        aliases = {"copypaste"},
        category = CommandCategory.CLAIMING,
        description = "Copy a plot",
        usage = "/plot copy <X;Z>",
        requiredType = RequiredType.NONE)
public class Copy extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        Location loc = plr.getLocation();
        Plot plot1 = loc.getPlotAbs();
        if (plot1 == null) {
            return !MainUtil.sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot1.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, C.PERMISSION_ADMIN.s())) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        Plot plot2 = MainUtil.getPlotFromString(plr, args[0], true);
        if (plot2 == null) {
            return false;
        }
        if (plot1.equals(plot2)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        if (!plot1.getArea().isCompatible(plot2.getArea())) {
            C.PLOTWORLD_INCOMPATIBLE.send(plr);
            return false;
        }
        if (plot1.copy(plot2, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, C.COPY_SUCCESS);
            }
        })) {
            return true;
        } else {
            MainUtil.sendMessage(plr, C.REQUIRES_UNOWNED);
            return false;
        }
    }
}
