package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "area",
        permission = "plots.area",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Plotarea commands",
        aliases = "world",
        usage = "/plot area <create|info|list|tp|regen>",
        confirmation = true)
public class Area extends Command {

    public Area() { super(MainCommand.getInstance(), true); }

}
