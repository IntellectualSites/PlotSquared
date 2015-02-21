package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;

/**
 * Created 2014-11-09 for PlotSquared
 *
 * @author Citymonstret
 */
public class OP extends SubCommand {
    public OP() {
        super(Command.OP, "Alias for /plot trusted add", "/plot op [player]", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            return MainUtil.sendMessage(plr, "&cUsage: &c" + this.usage);
        }
        if (!BukkitPlayerFunctions.isInPlot(plr)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        final Plot plot = BukkitPlayerFunctions.getCurrentPlot(plr);
        if (!plot.hasRights(plr)) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        return plr.performCommand("plot trusted add " + args[0]);
    }
}
