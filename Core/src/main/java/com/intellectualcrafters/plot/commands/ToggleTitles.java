package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "titles",
        permission = "plots.toggle.titles",
        usage = "/plot toggle clear-confirmation",
        description = "Toggle plot title messages",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class ToggleTitles extends ToggleCommand {

    public ToggleTitles(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public String toggleKey() {
        return "disabletitles";
    }
}
