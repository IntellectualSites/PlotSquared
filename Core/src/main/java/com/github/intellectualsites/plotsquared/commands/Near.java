package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal2;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal3;
import com.github.intellectualsites.plotsquared.util.StringMan;

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
