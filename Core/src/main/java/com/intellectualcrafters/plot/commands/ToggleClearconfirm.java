package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "clear-confirmation",
        permission = "plots.admin.command.autoclear",
        usage = "/plot toggle clear-confirmation",
        description = "Toggle autoclear confirmation",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class ToggleClearconfirm extends ToggleCommand {

    public ToggleClearconfirm(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public String toggleKey() {
        return "ignoreExpireTask";
    }
}