package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;

import java.util.ArrayList;

@CommandDeclaration(command = "debugsavetest",
    permission = "plots.debugsavetest",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.CONSOLE,
    usage = "/plot debugsavetest",
    description = "This command will force the recreation of all plots in the DB")
public class DebugSaveTest extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        ArrayList<Plot> plots = new ArrayList<>(PlotSquared.get().getPlots());
        MainUtil.sendMessage(player, "&6Starting `DEBUGSAVETEST`");
        DBFunc.createPlotsAndData(plots,
            () -> MainUtil.sendMessage(player, "&6Database sync finished!"));
        return true;
    }
}
