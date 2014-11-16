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
public class OP extends SubCommand {

    public OP() {
        super(Command.OP, "Alis for /plot trusted add", "/plot op [player]", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(Player plr, String... args) {
        if (args.length < 1) {
            return PlayerFunctions.sendMessage(plr, "&cUsage: &c" + usage);
        }
        if (!PlayerFunctions.isInPlot(plr)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!plot.hasRights(plr)) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        return plr.performCommand("plot trusted add " + args[0]);
    }
}
