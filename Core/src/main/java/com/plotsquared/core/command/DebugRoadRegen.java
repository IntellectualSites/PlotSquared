/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.generator.HybridPlotManager;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.util.MainUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;

@CommandDeclaration(command = "debugroadregen",
    usage = DebugRoadRegen.USAGE,
    requiredType = RequiredType.NONE,
    description = "Regenerate roads in the plot or region the user is, based on the road schematic",
    category = CommandCategory.DEBUG,
    permission = "plots.debugroadregen")
public class DebugRoadRegen extends SubCommand {

    public static final String USAGE = "/plot debugroadregen <plot|region [height]>";

    private final HybridUtils hybridUtils;

    @Inject public DebugRoadRegen(@Nonnull final HybridUtils hybridUtils) {
        this.hybridUtils = hybridUtils;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
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
        boolean result = this.hybridUtils.scheduleSingleRegionRoadUpdate(plot, height);
        if (!result) {
            MainUtil.sendMessage(player,
                "&cCannot schedule mass schematic update! (Is one already in progress?)");
            return false;
        }
        return true;
    }
}
