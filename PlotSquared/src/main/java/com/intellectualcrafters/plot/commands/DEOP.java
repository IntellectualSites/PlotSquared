package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import org.bukkit.entity.Player;

/**
 * Created 2014-11-09 for PlotSquared
 *
 * @author Citymonstret
 */
public class DEOP extends SubCommand {

    public DEOP() {
        super(Command.DEOP, "Alis for /plot trusted remove", "/plot deop  [player]", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length < 1) {
            return PlayerFunctions.sendMessage(plr, "&cUsage: &c" + this.usage);
        }
        if (!PlayerFunctions.isInPlot(plr)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!plot.hasRights(plr)) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        return plr.performCommand("plot trusted remove " + args[0]);
    }
}
