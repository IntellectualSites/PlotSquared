package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "alias",
        permission = "plots.alias",
        description = "Alias commands",
        usage = "/plot alias <set|remove>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Alias extends Command {

    public Alias() { super(MainCommand.getInstance(), true); }
}
