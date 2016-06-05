package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
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
    public boolean onCommand(PlotPlayer player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.continue")) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (!plot.hasFlag(Flags.DONE)) {
            MainUtil.sendMessage(player, C.DONE_NOT_DONE);
            return false;
        }
        int size = plot.getConnectedPlots().size();
        if (Settings.DONE.COUNTS_TOWARDS_LIMIT && (player.getAllowedPlots() < player.getPlotCount() + size)) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.admin.command.continue");
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
            return false;
        }
        plot.removeFlag(Flags.DONE);
        MainUtil.sendMessage(player, C.DONE_REMOVED);
        return true;
    }
}
