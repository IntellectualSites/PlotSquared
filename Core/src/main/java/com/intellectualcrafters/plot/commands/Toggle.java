package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "toggle",
        aliases = {"attribute"},
        permission = "plots.use",
        usage = "/plot toggle <chat|chatspy|clear-confirmation|titles|worldedit>",
        description = "Toggle per user settings",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class Toggle extends Command {

    public Toggle() {
        super(MainCommand.getInstance(), true);
    }

}
