package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.*;

@CommandDeclaration(command = "delete", permission = "plots.delete", description = "Delete a plot", usage = "/plot delete", aliases = {
    "dispose", "del",
    "reset"}, category = CommandCategory.CLAIMING, requiredType = RequiredType.NONE, confirmation = true)
public class Delete extends SubCommand {

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
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_DELETE)) {
            return !sendMessage(player, C.NO_PLOT_PERMS);
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        final int currentPlots =
            Settings.Limit.GLOBAL ? player.getPlotCount() : player.getPlotCount(loc.getWorld());
        Runnable run = new Runnable() {
            @Override public void run() {
                if (plot.getRunning() > 0) {
                    MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
                    return;
                }
                final long start = System.currentTimeMillis();
                boolean result = plot.deletePlot(new Runnable() {
                    @Override public void run() {
                        plot.removeRunning();
                        if ((EconHandler.manager != null) && plotArea.USE_ECONOMY) {
                            Expression<Double> valueExr = plotArea.PRICES.get("sell");
                            double value = plots.size() * valueExr.evaluate((double) currentPlots);
                            if (value > 0d) {
                                EconHandler.manager.depositMoney(player, value);
                                sendMessage(player, C.ADDED_BALANCE, String.valueOf(value));
                            }
                        }
                        MainUtil.sendMessage(player, C.DELETING_DONE,
                            System.currentTimeMillis() - start);
                    }
                });
                if (result) {
                    plot.addRunning();
                } else {
                    MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
                }
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
