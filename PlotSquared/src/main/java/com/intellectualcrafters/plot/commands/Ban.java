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
public class Ban extends SubCommand {
    public Ban() {
        super(Command.BAN, "Alias for /plot denied add", "/plot ban [player]", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (args.length < 1) {
            return MainUtil.sendMessage(plr, "&cUsage: &c" + this.usage);
        }
        Plot plot = MainUtil.getPlot(plr.getLocation());
        if (plot == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.isAdded(plr.getUUID())) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        
        return plr.performCommand("plot denied add " + args[0]);
    }
}
