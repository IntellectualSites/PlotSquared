package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "delete",
        permission = "plots.delete",
        description = "Delete a plot",
        usage = "/plot delete",
        aliases = {"dispose", "del"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class Delete extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {

        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, C.PLOT_UNOWNED);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.delete")) {
            return !sendMessage(player, C.NO_PLOT_PERMS);
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (plot.getRunning() > 0) {
                    MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
                    return;
                }
                final long start = System.currentTimeMillis();
                boolean result = plot.deletePlot(new Runnable() {
                    @Override
                    public void run() {
                        plot.removeRunning();
                        if ((EconHandler.manager != null) && plotArea.USE_ECONOMY) {
                            double value = plotArea.PRICES.get("sell") * plots.size();
                            if (value > 0d) {
                                EconHandler.manager.depositMoney(player, value);
                                sendMessage(player, C.ADDED_BALANCE, String.valueOf(value));
                            }
                        }
                        MainUtil.sendMessage(player, C.CLEARING_DONE, System.currentTimeMillis() - start);
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
