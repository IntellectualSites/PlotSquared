package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "near", aliases = "n", description = "Display nearby players", usage = "/plot near", category = CommandCategory.INFO)
public class Near extends Command {
    public Near() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), C.NOT_IN_PLOT);
        C.PLOT_NEAR.send(player, StringMan.join(plot.getPlayersInPlot(), ", "));
    }
}
