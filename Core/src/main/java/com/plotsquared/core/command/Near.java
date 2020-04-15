package com.plotsquared.core.command;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.StringMan;

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
