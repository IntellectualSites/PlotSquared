package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotManager;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

import java.util.Arrays;

@CommandDeclaration(command = "debugroadregen", usage = DebugRoadRegen.USAGE, requiredType = RequiredType.NONE, description = "Regenerate roads in the plot or region the user is, based on the road schematic", category = CommandCategory.DEBUG, permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {
    public static final String USAGE = "/plot debugroadregen <plot|region [height]>";

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        if (args.length < 1) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, DebugRoadRegen.USAGE);
            return false;
        }
        String kind = args[0].toLowerCase();
        switch (kind) {
            case "plot":
                return regenPlot(player);
            case "region":
                return regenRegion(player, Arrays.copyOfRange(args, 1, args.length));
            default:
                MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, DebugRoadRegen.USAGE);
                return false;
        }
    }

    public boolean regenPlot(PlotPlayer player) {
        Location location = player.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            Captions.NOT_IN_PLOT.send(player);
        } else if (plot.isMerged()) {
            Captions.REQUIRES_UNMERGED.send(player);
        } else {
            PlotManager manager = area.getPlotManager();
            manager.createRoadEast(plot);
            manager.createRoadSouth(plot);
            manager.createRoadSouthEast(plot);
            MainUtil.sendMessage(player, "&6Regenerating plot south/east roads: " + plot.getId()
                + "\n&6 - Result: &aSuccess");
            MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        }
        return true;
    }

    public boolean regenRegion(PlotPlayer player, String[] args) {
        int height = 0;
        if (args.length == 1) {
            try {
                height = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                MainUtil.sendMessage(player, Captions.NOT_VALID_NUMBER, "(0, 256)");
                MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, DebugRoadRegen.USAGE);
                return false;
            }
        } else if (args.length != 0) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, DebugRoadRegen.USAGE);
            return false;
        }

        Location location = player.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        Plot plot = player.getCurrentPlot();
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD);
            return true;
        }
        MainUtil
            .sendMessage(player, "&cIf no schematic is set, the following will not do anything");
        MainUtil.sendMessage(player,
            "&7 - To set a schematic, stand in a plot and use &c/plot createroadschematic");
        MainUtil.sendMessage(player, "&cTo regenerate all roads: /plot regenallroads");
        boolean result = HybridUtils.manager.scheduleSingleRegionRoadUpdate(plot, height);
        if (!result) {
            MainUtil.sendMessage(player,
                "&cCannot schedule mass schematic update! (Is one already in progress?)");
            return false;
        }
        return true;
    }
}
