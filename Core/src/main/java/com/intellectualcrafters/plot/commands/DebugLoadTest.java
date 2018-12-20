package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "debugloadtest",
        permission = "plots.debugloadtest",
        description = "This debug command will force the reload of all plots in the DB",
        usage = "/plot debugloadtest",
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.CONSOLE)
public class DebugLoadTest extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        PS.get().plots_tmp = DBFunc.getPlots();
        return true;
    }
}
