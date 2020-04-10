package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.TeleportCause;

/**
 * @author manuelgu, altered by Citymonstret
 */
@CommandDeclaration(command = "middle",
    aliases = {"center", "centre"},
    description = "Teleports you to the center of the plot",
    usage = "/plot middle",
    category = CommandCategory.TELEPORT,
    requiredType = RequiredType.PLAYER)
public class Middle extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] arguments) {
        Location location = player.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }
        plot.getCenter(center -> player.teleport(center, TeleportCause.COMMAND));
        return true;
    }
}
