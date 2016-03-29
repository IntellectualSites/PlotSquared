package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;

@CommandDeclaration(
        command = "debugsavetest",
        permission = "plots.debugsavetest",
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.CONSOLE,
        usage = "/plot debugsavetest",
        description = "This command will force the recreation of all plots in the DB")
public class DebugSaveTest extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        ArrayList<Plot> plots = new ArrayList<Plot>();
        plots.addAll(PS.get().getPlots());
        MainUtil.sendMessage(plr, "&6Starting `DEBUGSAVETEST`");
        DBFunc.createPlotsAndData(plots, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(plr, "&6Database sync finished!");
            }
        });
        return true;
    }
}
