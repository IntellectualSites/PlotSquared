package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.events.PlotFlagRemoveEvent;
import com.github.intellectualsites.plotsquared.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.Permissions;

@CommandDeclaration(command = "continue",
    description = "Continue a plot that was previously marked as done",
    permission = "plots.continue",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Continue extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Plot plot = player.getCurrentPlot();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_CONTINUE)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (!DoneFlag.isDone(plot)) {
            MainUtil.sendMessage(player, Captions.DONE_NOT_DONE);
            return false;
        }
        int size = plot.getConnectedPlots().size();
        if (Settings.Done.COUNTS_TOWARDS_LIMIT && (player.getAllowedPlots()
            < player.getPlotCount() + size)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                Captions.PERMISSION_ADMIN_COMMAND_CONTINUE);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(DoneFlag.class);
        PlotFlagRemoveEvent event =
            PlotSquared.get().getEventDispatcher().callFlagRemove(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Done flag removal");
            return true;
        }
        plot.removeFlag(event.getFlag());
        MainUtil.sendMessage(player, Captions.DONE_REMOVED);
        return true;
    }
}
