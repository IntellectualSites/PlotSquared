package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "weanywhere",
        permission = "plots.worldedit.bypass",
        description = "Force bypass of WorldEdit",
        aliases = {"wea"},
        usage = "/plot weanywhere",
        requiredType = RequiredType.NONE,
        category = CommandCategory.ADMINISTRATION)
@Deprecated
public class WE_Anywhere extends ToggleWorldedit {

    public WE_Anywhere() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) { return super.onCommand(player, args); }
}
