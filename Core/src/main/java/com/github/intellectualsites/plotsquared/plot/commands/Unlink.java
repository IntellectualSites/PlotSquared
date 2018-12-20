package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.*;

@CommandDeclaration(command = "unlink", aliases = {"u",
    "unmerge"}, description = "Unlink a mega-plot", usage = "/plot unlink", requiredType = RequiredType.NONE, category = CommandCategory.SETTINGS, confirmation = true)
public class Unlink extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {

        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, C.PLOT_UNOWNED);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_UNLINK)) {
            return sendMessage(player, C.NO_PLOT_PERMS);
        }
        if (!plot.isMerged()) {
            return sendMessage(player, C.UNLINK_IMPOSSIBLE);
        }
        final boolean createRoad;
        if (args.length != 0) {
            if (args.length != 1 || !StringMan.isEqualIgnoreCaseToAny(args[0], "true", "false")) {
                C.COMMAND_SYNTAX.send(player, getUsage());
                return false;
            }
            createRoad = Boolean.parseBoolean(args[0]);
        } else {
            createRoad = true;
        }
        Runnable runnable = new Runnable() {
            @Override public void run() {
                if (!plot.unlinkPlot(createRoad, createRoad)) {
                    MainUtil.sendMessage(player, "&cUnlink has been cancelled");
                    return;
                }
                MainUtil.sendMessage(player, C.UNLINK_SUCCESS);
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, "/plot unlink " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
