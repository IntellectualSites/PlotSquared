package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.expiry.ExpireManager;
import com.intellectualcrafters.plot.util.expiry.PlotAnalysis;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "done",
        aliases = {"submit"},
        description = "Mark a plot as done",
        permission = "plots.done",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Done extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.done")) {
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
                @Override
                public void run(PlotAnalysis value) {
                    plot.removeRunning();
                    boolean result = value.getComplexity(doneRequirements) <= doneRequirements.THRESHOLD;
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
