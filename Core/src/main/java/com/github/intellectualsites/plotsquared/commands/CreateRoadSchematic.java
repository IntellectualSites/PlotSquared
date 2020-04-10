package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.MainUtil;

@CommandDeclaration(command = "createroadschematic",
    aliases = {"crs"},
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.PLAYER,
    permission = "plots.createroadschematic",
    description = "Add a road schematic to your world using the roads around your current plot",
    usage = "/plot createroadschematic")
public class CreateRoadSchematic extends SubCommand {

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
        MainUtil.sendMessage(player, Captions.SCHEMATIC_ROAD_CREATED);
        return true;
    }
}
