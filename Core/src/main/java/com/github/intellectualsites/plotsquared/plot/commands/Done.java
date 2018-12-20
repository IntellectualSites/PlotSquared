package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;

@CommandDeclaration(command = "done", aliases = {
    "submit"}, description = "Mark a plot as done", permission = "plots.done", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE)
public class Done extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_DONE)) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.hasFlag(Flags.DONE)) {
            MainUtil.sendMessage(player, C.DONE_ALREADY_DONE);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        MainUtil.sendMessage(player, C.GENERATING_LINK);
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
        if (success) {
            long flagValue = System.currentTimeMillis() / 1000;
            plot.setFlag(Flags.DONE, flagValue);
            MainUtil.sendMessage(pp, C.DONE_SUCCESS);
        } else {
            MainUtil.sendMessage(pp, C.DONE_INSUFFICIENT_COMPLEXITY);
        }
    }
}
