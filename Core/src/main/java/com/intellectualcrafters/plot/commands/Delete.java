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
import java.util.HashSet;

@CommandDeclaration(
        command = "delete",
        permission = "plots.delete",
        description = "Delete a plot",
        usage = "/plot delete",
        aliases = {"dispose", "del"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        confirmation=true)
public class Delete extends SubCommand {

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
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.delete")) {
            return !sendMessage(plr, C.NO_PLOT_PERMS);
        }
        final PlotArea plotworld = plot.getArea();
        final HashSet<Plot> plots = plot.getConnectedPlots();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (plot.getRunning() > 0) {
                    MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                    return;
                }
                final long start = System.currentTimeMillis();
                boolean result = plot.deletePlot(new Runnable() {
                    @Override
                    public void run() {
                        plot.removeRunning();
                        if ((EconHandler.manager != null) && plotworld.USE_ECONOMY) {
                            double value = plotworld.PRICES.get("sell") * plots.size();
                            if (value > 0d) {
                                EconHandler.manager.depositMoney(plr, value);
                                sendMessage(plr, C.ADDED_BALANCE, value + "");
                            }
                        }
                        MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                    }
                });
                if (result) {
                    plot.addRunning();
                } else {
                    MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                }
            }
        };
        if (hasConfirmation(plr)) {
            CmdConfirm.addPending(plr, "/plot delete " + plot.getId(), run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }
}
