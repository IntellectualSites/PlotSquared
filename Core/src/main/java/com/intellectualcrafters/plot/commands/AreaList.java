package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;


@CommandDeclaration(command = "list",
        permission = "plots.area.list",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Show a list of all current PlotAreas",
        aliases = "l",
        usage = "/plot area list [#]",
        confirmation = true)
public class AreaList extends SubCommand {

    public AreaList(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        int page;
        switch (args.length) {
            case 0:
                page = 0;
                break;
            case 1:
                if (MathMan.isInteger(args[1])) {
                    page = Integer.parseInt(args[1]) - 1;
                    break;
                }
            default:
                C.COMMAND_SYNTAX.send(player, getUsage());
                return false;
        }
        ArrayList<PlotArea> areas = new ArrayList<>(PS.get().getPlotAreas());
        paginate(player, areas, 8, page, new RunnableVal3<Integer, PlotArea, PlotMessage>() {
            @Override
            public void run(Integer i, PlotArea area, PlotMessage message) {
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
                    percent = claimed == 0 ? 0 : Short.MAX_VALUE * Short.MAX_VALUE / (double) claimed;
                    region = "N/A";
                }
                PlotMessage tooltip = new PlotMessage()
                        .text("Claimed=").color("$1").text(String.valueOf(claimed)).color("$2")
                        .text("\nUsage=").color("$1").text(String.format("%.2f", percent) + '%').color("$2")
                        .text("\nClusters=").color("$1").text(String.valueOf(clusters)).color("$2")
                        .text("\nRegion=").color("$1").text(region).color("$2")
                        .text("\nGenerator=").color("$1").text(generator).color("$2");

                // type / terrain
                String visit = "/plot area tp " + area.toString();
                message.text("[").color("$3")
                        .text(String.valueOf(i)).command(visit).tooltip(visit).color("$1")
                        .text("]").color("$3")
                        .text(' ' + name).tooltip(tooltip).command(getCommandString() + " info " + area).color("$1").text(" - ")
                        .color("$2")
                        .text(area.TYPE + ":" + area.TERRAIN).color("$3");
            }
        }, "/plot area list", C.AREA_LIST_HEADER_PAGED.s());
        return true;
    }
}
