package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "weanywhere", permission = "plots.worldedit.bypass", description = "Force bypass of WorldEdit", aliases = {
    "wea"}, usage = "/plot weanywhere", requiredType = RequiredType.NONE, category = CommandCategory.ADMINISTRATION)
@Deprecated public class WE_Anywhere extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] arguments) {
        MainCommand.getInstance().toggle.worldedit(this, player, new String[0], null, null);
        return true;
    }

}
