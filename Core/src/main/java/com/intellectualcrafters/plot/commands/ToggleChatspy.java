package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "chatspy",
        permission = "plots.admin.command.chat",
        aliases = {"spy"},
        usage = "/plot toggle chatspy",
        description = "Toggle admin chat spying",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class ToggleChatspy extends ToggleCommand {

    public ToggleChatspy(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public String toggleKey() {
        return "chatspy";
    }
}
