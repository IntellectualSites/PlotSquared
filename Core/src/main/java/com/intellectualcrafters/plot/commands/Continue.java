package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "continue",
        description = "Continue a plot that was previously marked as done",
        permission = "plots.continue",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Continue extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        Plot plot = plr.getCurrentPlot();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.continue")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (!plot.getFlags().containsKey("done")) {
            MainUtil.sendMessage(plr, C.DONE_NOT_DONE);
            return false;
        }
        int size = plot.getConnectedPlots().size();
        if (Settings.DONE_COUNTS_TOWARDS_LIMIT && (plr.getAllowedPlots() < plr.getPlotCount() + size)) {
            MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.admin.command.continue");
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        FlagManager.removePlotFlag(plot, "done");
        MainUtil.sendMessage(plr, C.DONE_REMOVED);
        return true;
    }
}
