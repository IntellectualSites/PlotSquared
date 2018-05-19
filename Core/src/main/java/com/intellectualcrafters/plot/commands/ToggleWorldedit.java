package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "worldedit",
        permission = "plots.worldedit.bypass",
        aliases = {"we", "wea"},
        usage = "/plot toggle worldedit",
        description = "Toggle worldedit area restrictions",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class ToggleWorldedit extends ToggleCommand {

    public ToggleWorldedit(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public String toggleKey() {
        return "worldedit";
    }
}
