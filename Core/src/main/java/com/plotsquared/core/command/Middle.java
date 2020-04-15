package com.plotsquared.core.command;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.events.TeleportCause;

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
