package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "description",
        permission = "plots.set.desc",
        description = "Set the plot description",
        usage = "/plot set description [description]",
        aliases = {"desc"},
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.NONE)
public class SetDescription extends SetCommand {

    public SetDescription(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean set(final PlotPlayer player, final Plot plot, final String value) {
        if (value.isEmpty()) {
            plot.removeFlag(Flags.DESCRIPTION);
            MainUtil.sendMessage(player, C.DESC_UNSET);
            return true;
        }
        boolean result = FlagManager.addPlotFlag(plot, Flags.DESCRIPTION, value);
        if (!result) {
            MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
            return false;
        }
        MainUtil.sendMessage(player, C.DESC_SET);
        return true;
    }
}