package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "debugroadregen",
        usage = "/plot debugroadregen",
        requiredType = RequiredType.NONE,
        description = "Regenerate all roads based on the road schematic",
        category = CommandCategory.DEBUG,
        permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        PlotArea plotArea = loc.getPlotArea();
        if (!(plotArea instanceof HybridPlotWorld)) {
            return sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            ChunkLoc chunk = new ChunkLoc(loc.getX() >> 4, loc.getZ() >> 4);
            int extend = 0;
            if (args.length == 1) {
                if (MathMan.isInteger(args[0])) {
                    try {
                        extend = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ignored) {
                        C.NOT_VALID_NUMBER.send(player, "(0, <EXTEND HEIGHT>)");
                        return false;
                    }
                }
            }
            boolean result = HybridUtils.manager.regenerateRoad(plotArea, chunk, extend);
            MainUtil.sendMessage(player,
                    "&6Regenerating chunk: " + chunk.x + ',' + chunk.z + "\n&6 - Result: " + (result ? "&aSuccess" : "&cFailed"));
            MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        } else if (plot.isMerged()) {
            C.REQUIRES_UNMERGED.send(player);
        } else {
            HybridPlotManager manager = (HybridPlotManager) plotArea.getPlotManager();
            manager.createRoadEast(plotArea, plot);
            manager.createRoadSouth(plotArea, plot);
            manager.createRoadSouthEast(plotArea, plot);
            MainUtil.sendMessage(player, "&6Regenerating plot south/east roads: " + plot.getId() + "\n&6 - Result: &aSuccess");
            MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        }
        return true;
    }
}
