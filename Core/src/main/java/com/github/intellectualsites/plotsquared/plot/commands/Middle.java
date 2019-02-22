package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

/**
 * @author manuelgu, altered by Citymonstret
 */
@CommandDeclaration(command = "middle", aliases = {"center", "centre"},
    description = "Teleports you to the center of the plot", usage = "/plot middle",
    category = CommandCategory.TELEPORT, requiredType = RequiredType.NONE) public class Middle
    extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] arguments) {
        Location location = player.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return sendMessage(player, Captions.PLOT_UNOWNED);
        }
        player.teleport(plot.getCenter());
        return true;
    }
}
