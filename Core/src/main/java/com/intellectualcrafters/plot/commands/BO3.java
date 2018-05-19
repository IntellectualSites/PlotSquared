package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "bo3",
        aliases = {"bo2"},
        description = "Import or export a plot",
        permission = "plots.bo3",
        usage = "/plot bo3 <export|import>",
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE)
public class BO3 extends Command {

    public BO3() { super(MainCommand.getInstance(), true); }

}
