package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@CommandDeclaration(command = "info",
        permission = "plots.area.info",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Get information about a single PlotArea",
        aliases = "i",
        usage = "/plot area info <area>")
public class AreaInfo extends SubCommand {

    public AreaInfo(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 0) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        if (!Permissions.hasPermission(player, C.PERMISSION_AREA_INFO)) {
            C.NO_PERMISSION.send(player, C.PERMISSION_AREA_INFO);
            return false;
        }
        PlotArea area;
        switch (args.length) {
            case 1:
                area = player.getApplicablePlotArea();
                break;
            case 2:
                area = PS.get().getPlotAreaByString(args[1]);
                break;
            default:
                C.COMMAND_SYNTAX.send(player, getUsage());
                return false;
        }
        if (area == null) {
            if (args.length == 2) {
                C.NOT_VALID_PLOT_WORLD.send(player, args[1]);
            } else {
                C.NOT_IN_PLOT_WORLD.send(player);
            }
            return false;
        }
        String name;
        double percent;
        int claimed = area.getPlotCount();
        int clusters = area.getClusters().size();
        String region;
        String generator = String.valueOf(area.getGenerator());
        if (area.TYPE == 2) {
            PlotId min = area.getMin();
            PlotId max = area.getMax();
            name = area.worldname + ';' + area.id + ';' + min + ';' + max;
            int size = (max.x - min.x + 1) * (max.y - min.y + 1);
            percent = claimed == 0 ? 0 : size / (double) claimed;
            region = area.getRegion().toString();
        } else {
            name = area.worldname;
            percent = claimed == 0 ? 0 : 100d * claimed / Integer.MAX_VALUE;
            region = "N/A";
        }
        String value = "&r$1NAME: " + name
                + "\n$1Type: $2" + area.TYPE
                + "\n$1Terrain: $2" + area.TERRAIN
                + "\n$1Usage: $2" + String.format("%.2f", percent) + '%'
                + "\n$1Claimed: $2" + claimed
                + "\n$1Clusters: $2" + clusters
                + "\n$1Region: $2" + region
                + "\n$1Generator: $2" + generator;
        MainUtil.sendMessage(player, C.PLOT_INFO_HEADER.s() + '\n' + value + '\n' + C.PLOT_INFO_FOOTER.s(), false);
        return true;
    }
}