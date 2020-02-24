package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

@CommandDeclaration(command = "debugloadtest",
    permission = "plots.debugloadtest",
    description = "This debug command will force the reload of all plots in the DB",
    usage = "/plot debugloadtest",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.CONSOLE)
public class DebugLoadTest extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        PlotSquared.get().plots_tmp = DBFunc.getPlots();
        return true;
    }
}
