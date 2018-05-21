package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "setflag",
        permission = "plots.set.flag",
        description = "Set the plot flags",
        usage = "/plot setflag <flag> <value>",
        aliases = {"setf"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Setflag extends FlagSet {

    public Setflag() { super(MainCommand.getInstance(), true); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) { return super.onCommand(player, args); }
}
