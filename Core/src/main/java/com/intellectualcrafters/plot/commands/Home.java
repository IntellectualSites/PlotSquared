package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "sethome",
        permission = "plots.set.home",
        description = "Set the plot home",
        usage = "/plot sethome [none]",
        aliases = {"sh", "seth"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Home extends SetHome {

    public Home() { super(MainCommand.getInstance(), true); }

    @Override
    public boolean set(PlotPlayer player, Plot plot, String value) {
        return super.set(player, plot, value);
    }
}