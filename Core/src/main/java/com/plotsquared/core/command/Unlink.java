package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.TaskManager;

@CommandDeclaration(command = "unlink",
    aliases = {"u", "unmerge"},
    description = "Unlink a mega-plot",
    usage = "/plot unlink [createroads]",
    requiredType = RequiredType.PLAYER,
    category = CommandCategory.SETTINGS,
    confirmation = true)
public class Unlink extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, Captions.PLOT_UNOWNED);
        }
        if (!plot.isMerged()) {
            return sendMessage(player, Captions.UNLINK_IMPOSSIBLE);
        }
        final boolean createRoad;
        if (args.length != 0) {
            if (args.length != 1 || !StringMan.isEqualIgnoreCaseToAny(args[0], "true", "false")) {
                Captions.COMMAND_SYNTAX.send(player, getUsage());
                return false;
            }
            createRoad = Boolean.parseBoolean(args[0]);
        } else {
            createRoad = true;
        }

        PlotUnlinkEvent event = PlotSquared.get().getEventDispatcher()
            .callUnlink(plot.getArea(), plot, createRoad, createRoad,
                PlotUnlinkEvent.REASON.PLAYER_COMMAND);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Unlink");
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_UNLINK)) {
            return sendMessage(player, Captions.NO_PLOT_PERMS);
        }
        Runnable runnable = () -> {
            if (!plot.unlinkPlot(createRoad, createRoad)) {
                MainUtil.sendMessage(player, Captions.UNMERGE_CANCELLED);
                return;
            }
            MainUtil.sendMessage(player, Captions.UNLINK_SUCCESS);
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, "/plot unlink " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
