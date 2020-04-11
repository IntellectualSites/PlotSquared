package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.tasks.RunnableVal2;
import com.plotsquared.util.tasks.RunnableVal3;
import com.plotsquared.util.StringMan;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "near",
    aliases = "n",
    description = "Display nearby players",
    usage = "/plot near",
    category = CommandCategory.INFO,
    requiredType = RequiredType.PLAYER)
public class Near extends Command {
    public Near() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        Captions.PLOT_NEAR.send(player, StringMan.join(plot.getPlayersInPlot(), ", "));
        return CompletableFuture.completedFuture(true);
    }
}
