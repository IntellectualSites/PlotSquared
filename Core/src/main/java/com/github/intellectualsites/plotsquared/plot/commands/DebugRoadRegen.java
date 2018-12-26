package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotManager;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;

@CommandDeclaration(command = "debugroadregen", usage = "/plot debugroadregen", requiredType = RequiredType.NONE, description = "Regenerate all roads based on the road schematic", category = CommandCategory.DEBUG, permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        PlotArea plotArea = loc.getPlotArea();
        if (!(plotArea instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
           C.NOT_IN_PLOT.send(player);
        } else {
            HybridPlotManager manager = (HybridPlotManager) plotArea.getPlotManager();
            manager.createRoadEast(plotArea, plot);
            manager.createRoadSouth(plotArea, plot);
            manager.createRoadSouthEast(plotArea, plot);
            MainUtil.sendMessage(player, "&6Regenerating plot south/east roads: " + plot.getId()
                + "\n&6 - Result: &aSuccess");
            MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        }
        return true;
    }
}
