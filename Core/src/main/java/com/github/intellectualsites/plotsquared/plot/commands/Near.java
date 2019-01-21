package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "near", aliases = "n", description = "Display nearby players",
    usage = "/plot near", category = CommandCategory.INFO) public class Near extends Command {
    public Near() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), C.NOT_IN_PLOT);
        C.PLOT_NEAR.send(player, StringMan.join(plot.getPlayersInPlot(), ", "));
        return CompletableFuture.completedFuture(true);
    }
}
