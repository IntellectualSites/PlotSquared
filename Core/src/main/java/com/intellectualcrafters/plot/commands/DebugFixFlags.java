package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

@CommandDeclaration(
        command = "debugfixflags",
        usage = "/plot debugfixflags <world>",
        permission = "plots.debugfixflags",
        description = "Attempt to fix all flags for a world",
        requiredType = RequiredType.CONSOLE,
        category = CommandCategory.DEBUG)
public class DebugFixFlags extends SubCommand {

    public DebugFixFlags() {
        super(Argument.String);
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
            MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_WORLD, args[0]);
            return false;
        }
        MainUtil.sendMessage(plr, "&8--- &6Starting task &8 ---");
        for (Plot plot : area.getPlots()) {
            HashMap<String, Flag> flags = plot.getFlags();
            Iterator<Entry<String, Flag>> i = flags.entrySet().iterator();
            boolean changed = false;
            while (i.hasNext()) {
                if (FlagManager.getFlag(i.next().getKey()) == null) {
                    changed = true;
                    i.remove();
                }
            }
            if (changed) {
                DBFunc.setFlags(plot, plot.getFlags().values());
            }
        }
        MainUtil.sendMessage(plr, "&aDone!");
        return true;
    }
}
