package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "template",
        permission = "plots.admin",
        description = "Create or use a world template",
        usage = "/plot template <import|export>",
        category = CommandCategory.ADMINISTRATION)
public class Template extends Command {

    public Template() {
        super(MainCommand.getInstance(), true);
    }

}
