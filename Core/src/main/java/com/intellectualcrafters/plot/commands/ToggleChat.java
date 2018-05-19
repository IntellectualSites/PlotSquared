package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "chat",
        permission = "plots.toggle.chat",
        usage = "/plot toggle chat",
        description = "Toggle plot chat",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class ToggleChat extends ToggleCommand {

    public ToggleChat(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public String toggleKey() {
        return "chat";
    }
}