package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

@CommandDeclaration(command = "debug", category = CommandCategory.DEBUG, description = "Show debug information", usage = "/plot debug [msg]", permission = "plots.admin")
public class Debug extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            StringBuilder msg = new StringBuilder();
            for (C caption : C.values()) {
                msg.append(caption.s()).append("\n");
            }
            MainUtil.sendMessage(player, msg.toString());
            return true;
        }
        StringBuilder information = new StringBuilder();
        String header = C.DEBUG_HEADER.s();
        String line = C.DEBUG_LINE.s();
        String section = C.DEBUG_SECTION.s();
        information.append(header);
        information.append(getSection(section, "PlotArea"));
        information
            .append(getLine(line, "Plot Worlds", StringMan.join(PlotSquared.get().getPlotAreas(), ", ")));
        information.append(getLine(line, "Owned Plots", PlotSquared.get().getPlots().size()));
        information.append(getSection(section, "Messages"));
        information.append(getLine(line, "Total Messages", C.values().length));
        information.append(getLine(line, "View all captions", "/plot debug msg"));
        MainUtil.sendMessage(player, information.toString());
        return true;
    }

    private String getSection(String line, String val) {
        return line.replaceAll("%val%", val) + "\n";
    }

    private String getLine(String line, String var, Object val) {
        return line.replaceAll("%var%", var).replaceAll("%val%", "" + val) + "\n";
    }
}
