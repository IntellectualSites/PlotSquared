package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.object.Expression;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;


@CommandDeclaration(command = "delete",
    permission = "plots.delete",
    description = "Delete the plot you stand on",
    usage = "/plot delete",
    aliases = {"dispose", "del", "unclaim"},
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Delete extends SubCommand {

    // Note: To delete a specific plot use /plot <plot> delete
    // The syntax also works with any command: /plot <plot> <command>

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, Captions.PLOT_UNOWNED);
        }
        Result eventResult = PlotSquared.get().getEventDispatcher().callDelete(plot).getEventResult();
        if (eventResult == Result.DENY) {
            player.sendMessage(CaptionUtility.format(player, eventResult.getReason()));
            return true;
        }
        boolean force = eventResult == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DELETE)) {
            return !sendMessage(player, Captions.NO_PLOT_PERMS);
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        final int currentPlots = Settings.Limit.GLOBAL ?
            player.getPlotCount() :
            player.getPlotCount(location.getWorld());
        Runnable run = () -> {
            if (plot.getRunning() > 0) {
                MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
                return;
            }
            final long start = System.currentTimeMillis();
            boolean result = plot.deletePlot(() -> {
                plot.removeRunning();
                if ((EconHandler.manager != null) && plotArea.USE_ECONOMY) {
                    Expression<Double> valueExr = plotArea.PRICES.get("sell");
                    double value = plots.size() * valueExr.evaluate((double) currentPlots);
                    if (value > 0d) {
                        EconHandler.manager.depositMoney(player, value);
                        sendMessage(player, Captions.ADDED_BALANCE, String.valueOf(value));
                    }
                }
                MainUtil.sendMessage(player, Captions.DELETING_DONE,
                    System.currentTimeMillis() - start);
            });
            if (result) {
                plot.addRunning();
            } else {
                MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, getCommandString() + ' ' + plot.getId(), run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
