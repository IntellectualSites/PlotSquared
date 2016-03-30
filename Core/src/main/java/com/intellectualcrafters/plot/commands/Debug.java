package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;
import org.apache.commons.lang.StringUtils;

@CommandDeclaration(command = "debug",
        category = CommandCategory.DEBUG,
        description = "Show debug information",
        usage = "/plot debug [msg]",
        permission = "plots.admin")
public class Debug extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            StringBuilder msg = new StringBuilder();
            for (C c : C.values()) {
                msg.append(c.s()).append("\n");
            }
            MainUtil.sendMessage(plr, msg.toString());
            return true;
        }
        StringBuilder information = new StringBuilder();
        String header = C.DEBUG_HEADER.s();
        String line = C.DEBUG_LINE.s();
        String section = C.DEBUG_SECTION.s();
        String worlds = StringUtils.join(PS.get().getPlotAreas(), ", ");
        information.append(header);
        information.append(getSection(section, "PlotArea"));
        information.append(getLine(line, "Plot Worlds", worlds));
        information.append(getLine(line, "Owned Plots", PS.get().getPlots().size()));
        information.append(getSection(section, "Messages"));
        information.append(getLine(line, "Total Messages", C.values().length));
        information.append(getLine(line, "View all captions", "/plot debug msg"));
        MainUtil.sendMessage(plr, information.toString());
        return true;
    }

    private String getSection(String line, String val) {
        return line.replaceAll("%val%", val) + "\n";
    }

    private String getLine(String line, String var, Object val) {
        return line.replaceAll("%var%", var).replaceAll("%val%", "" + val) + "\n";
    }
}
