package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.Map;

@CommandDeclaration(command = "debug", category = CommandCategory.DEBUG, description = "Show debug information", usage = "/plot debug [msg]", permission = "plots.admin")
public class Debug extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        if ((args.length > 0) && "player".equalsIgnoreCase(args[1])) {
            for (Map.Entry<String, Object> meta : player.getMeta().entrySet()) {
                MainUtil.sendMessage(player,
                    "Key: " + meta.getKey() + " Value: " + meta.getValue().toString() + " , ");
            }
        }
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            StringBuilder msg = new StringBuilder();
            for (Captions caption : Captions.values()) {
                msg.append(caption.getTranslated()).append("\n");
            }
            MainUtil.sendMessage(player, msg.toString());
            return true;
        }
        StringBuilder information = new StringBuilder();
        String header = Captions.DEBUG_HEADER.getTranslated();
        String line = Captions.DEBUG_LINE.getTranslated();
        String section = Captions.DEBUG_SECTION.getTranslated();
        information.append(header);
        information.append(getSection(section, "PlotArea"));
        information.append(
            getLine(line, "Plot Worlds", StringMan.join(PlotSquared.get().getPlotAreas(), ", ")));
        information.append(getLine(line, "Owned Plots", PlotSquared.get().getPlots().size()));
        information.append(getSection(section, "Messages"));
        information.append(getLine(line, "Total Messages", Captions.values().length));
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
