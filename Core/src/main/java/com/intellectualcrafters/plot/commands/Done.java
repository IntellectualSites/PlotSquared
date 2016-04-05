package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "done",
        aliases = {"submit"},
        description = "Mark a plot as done",
        permission = "plots.done",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class Done extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        Location loc = plr.getLocation();
        final Plot plot = loc.getPlotAbs();
        if ((plot == null) || !plot.hasOwner()) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.done")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getFlags().containsKey("done")) {
            MainUtil.sendMessage(plr, C.DONE_ALREADY_DONE);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        MainUtil.sendMessage(plr, C.GENERATING_LINK);
        HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
            @Override
            public void run(PlotAnalysis value) {
                plot.removeRunning();
                if ((value == null) || (value.getComplexity() >= Settings.CLEAR_THRESHOLD)) {
                    Flag flag = new Flag(FlagManager.getFlag("done"), System.currentTimeMillis() / 1000);
                    FlagManager.addPlotFlag(plot, flag);
                    MainUtil.sendMessage(plr, C.DONE_SUCCESS);
                } else {
                    MainUtil.sendMessage(plr, C.DONE_INSUFFICIENT_COMPLEXITY);
                }
            }
        });
        return true;
    }
}
