package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
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
