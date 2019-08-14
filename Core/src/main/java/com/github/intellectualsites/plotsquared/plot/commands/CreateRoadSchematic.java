package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

@CommandDeclaration(command = "createroadschematic", aliases = {"crs"},
    category = CommandCategory.ADMINISTRATION, requiredType = RequiredType.NONE,
    permission = "plots.createroadschematic",
    description = "Add a road schematic to your world using the roads around your current plot",
    usage = "/plot createroadschematic") public class CreateRoadSchematic extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!(location.getPlotArea() instanceof HybridPlotWorld)) {
            return sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        HybridUtils.manager.setupRoadSchematic(plot);
        MainUtil.sendMessage(player,
            "$1Saved new road schematic. To test the road, fly to a few other plots and use /plot debugroadregen");
        return true;
    }
}
