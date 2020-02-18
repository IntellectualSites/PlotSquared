package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

@CommandDeclaration(command = "debugfixflags", usage = "/plot debugfixflags <world>", permission = "plots.debugfixflags", description = "Attempt to fix all flags for a world", requiredType = RequiredType.CONSOLE, category = CommandCategory.DEBUG)
public class DebugFixFlags extends SubCommand {

    public DebugFixFlags() {
        super(Argument.String);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD, args[0]);
            return false;
        }
        MainUtil.sendMessage(player, "&8--- &6Starting task &8 ---");
        for (Plot plot : area.getPlots()) {
            HashMap<Flag<?>, Object> flags = plot.getFlags();
            Iterator<Entry<Flag<?>, Object>> i = flags.entrySet().iterator();
            boolean changed = false;
            while (i.hasNext()) {
                if (i.next().getKey() == null) {
                    changed = true;
                    i.remove();
                }
            }
            if (changed) {
                DBFunc.setFlags(plot, plot.getFlags());
            }
        }
        MainUtil.sendMessage(player, "&aDone!");
        return true;
    }
}
