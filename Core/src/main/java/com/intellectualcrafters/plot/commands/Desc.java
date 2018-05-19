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
        command = "setdescription",
        permission = "plots.set.desc",
        description = "Set the plot description",
        usage = "/plot setdescription <description>",
        aliases = {"desc", "setdesc", "setd", "description"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Desc extends SetDescription {

    public Desc() { super(MainCommand.getInstance(), true); }

    @Override
    public boolean set(PlotPlayer player, Plot plot, String value) {
        return super.set(player, plot, value);
    }
}