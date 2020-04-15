package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.events.PlotDoneEvent;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.expiration.PlotAnalysis;

@CommandDeclaration(command = "done",
    aliases = {"submit"},
    description = "Mark a plot as done",
    permission = "plots.done",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE)
public class Done extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        PlotDoneEvent event = PlotSquared.get().getEventDispatcher().callDone(plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Done");
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DONE)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (DoneFlag.isDone(plot)) {
            MainUtil.sendMessage(player, Captions.DONE_ALREADY_DONE);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        MainUtil.sendMessage(player, Captions.GENERATING_LINK);
        final Settings.Auto_Clear doneRequirements = Settings.AUTO_CLEAR.get("done");
        if (ExpireManager.IMP == null || doneRequirements == null) {
            finish(plot, player, true);
            plot.removeRunning();
        } else {
            HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                @Override public void run(PlotAnalysis value) {
                    plot.removeRunning();
                    boolean result =
                        value.getComplexity(doneRequirements) <= doneRequirements.THRESHOLD;
                    finish(plot, player, result);
                }
            });
        }
        return true;
    }

    private void finish(Plot plot, PlotPlayer pp, boolean success) {
        if (!success) {
            MainUtil.sendMessage(pp, Captions.DONE_INSUFFICIENT_COMPLEXITY);
            return;
        }
        long flagValue = System.currentTimeMillis() / 1000;
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(DoneFlag.class)
            .createFlagInstance(Long.toString(flagValue));
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(pp, Captions.EVENT_DENIED, "Done flag addition");
            return;
        }
        plot.setFlag(plotFlag);
        MainUtil.sendMessage(pp, Captions.DONE_SUCCESS);
    }
}
