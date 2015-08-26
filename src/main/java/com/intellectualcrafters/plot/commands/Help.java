package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "help",
        description = "Get this help menu",
        aliases = {"he"},
        category = CommandCategory.INFO
)
public class Help extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        return true;
    }
}
