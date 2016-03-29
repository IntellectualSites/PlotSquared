package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "setdescription",
        permission = "plots.set.desc",
        description = "Set the plot description",
        usage = "/plot desc <description>",
        aliases = {"desc", "setdesc", "setd", "description"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Desc extends SetCommand {

    @Override
    public boolean set(PlotPlayer plr, Plot plot, String desc) {
        if (desc.isEmpty()) {
            plot.removeFlag("description");
            MainUtil.sendMessage(plr, C.DESC_UNSET);
            return true;
        }
        Flag flag = new Flag(FlagManager.getFlag("description"), desc);
        boolean result = FlagManager.addPlotFlag(plot, flag);
        if (!result) {
            MainUtil.sendMessage(plr, C.FLAG_NOT_ADDED);
            return false;
        }
        MainUtil.sendMessage(plr, C.DESC_SET);
        return true;
    }
}
