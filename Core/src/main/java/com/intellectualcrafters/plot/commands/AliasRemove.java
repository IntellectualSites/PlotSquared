package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "remove",
        permission = "plots.alias.remove",
        description = "Remove the plot name",
        usage = "/plot alias remove",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class AliasRemove extends SubCommand {

    public AliasRemove(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }

        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_NOT_CLAIMED);
            return false;
        }

        if (!plot.isOwner(player.getUUID())) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }

        plot.setAlias(null);
        MainUtil.sendMessage(player, C.ALIAS_REMOVED.s());

        return true;
    }
}