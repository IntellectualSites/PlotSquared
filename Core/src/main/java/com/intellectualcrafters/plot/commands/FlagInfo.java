package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "info",
        permission = "plots.flag.info",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        description = "Get information on a plot flag",
        usage = "/plot flag info <flag>")
public class FlagInfo extends FlagCommand {

    public FlagInfo(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        Flag<?> flag = getFlag(player, plot, args[0]);
        if (flag == null) { return false; }
        // flag key
        MainUtil.sendMessage(player, C.FLAG_KEY, flag.getName());
        // flag type
        MainUtil.sendMessage(player, C.FLAG_TYPE, flag.getClass().getSimpleName());
        // Flag type description
        MainUtil.sendMessage(player, C.FLAG_DESC, flag.getValueDescription());
        return true;
    }
}
