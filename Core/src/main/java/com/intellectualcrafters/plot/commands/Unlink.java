package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "unlink",
        aliases = {"u", "unmerge"},
        description = "Unlink a mega-plot",
        usage = "/plot unlink",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS,
        confirmation = true)
public class Unlink extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {

        Location loc = plr.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(plr, C.PLOT_UNOWNED);
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.unlink")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        if (!plot.isMerged()) {
            return sendMessage(plr, C.UNLINK_IMPOSSIBLE);
        }
        final boolean createRoad;
        if (args.length != 0) {
            if (args.length != 1 || !StringMan.isEqualIgnoreCaseToAny(args[0], "true", "false")) {
                C.COMMAND_SYNTAX.send(plr, getUsage());
                return false;
            }
            createRoad = Boolean.parseBoolean(args[0]);
        } else {
            createRoad = true;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!plot.unlinkPlot(createRoad, createRoad)) {
                    MainUtil.sendMessage(plr, "&cUnlink has been cancelled");
                    return;
                }
                MainUtil.sendMessage(plr, C.UNLINK_SUCCESS);
            }
        };
        if (hasConfirmation(plr)) {
            CmdConfirm.addPending(plr, "/plot unlink " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
