package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

@CommandDeclaration(command = "continue", description = "Continue a plot that was previously marked as done", permission = "plots.continue", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE)
public class Continue extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_CONTINUE)) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (!plot.hasFlag(Flags.DONE)) {
            MainUtil.sendMessage(player, C.DONE_NOT_DONE);
            return false;
        }
        int size = plot.getConnectedPlots().size();
        if (Settings.Done.COUNTS_TOWARDS_LIMIT && (player.getAllowedPlots()
            < player.getPlotCount() + size)) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_ADMIN_COMMAND_CONTINUE);
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
