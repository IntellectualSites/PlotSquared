package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "grant",
        category = CommandCategory.CLAIMING,
        usage = "/plot grant <check|add> [player]",
        permission = "plots.grant",
        requiredType = RequiredType.NONE)
public class Grant extends Command {

    public Grant() {
        super(MainCommand.getInstance(), true);
    }

}
