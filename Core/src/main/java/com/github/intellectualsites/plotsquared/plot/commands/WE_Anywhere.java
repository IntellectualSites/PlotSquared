package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

@CommandDeclaration(command = "weanywhere", permission = "plots.worldedit.bypass",
    description = "Force bypass of WorldEdit restrictions", aliases = {"wea"}, usage = "/plot weanywhere",
    requiredType = RequiredType.NONE, category = CommandCategory.ADMINISTRATION) @Deprecated
public class WE_Anywhere extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] arguments) {
        MainCommand.getInstance().toggle.worldedit(this, player, new String[0], null, null);
        return true;
    }

}
