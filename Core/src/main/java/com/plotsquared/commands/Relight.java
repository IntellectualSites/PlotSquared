package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.tasks.RunnableVal;
import com.plotsquared.util.tasks.RunnableVal2;
import com.plotsquared.util.tasks.RunnableVal3;
import com.plotsquared.util.ChunkManager;
import com.plotsquared.queue.LocalBlockQueue;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "relight",
    description = "Relight your plot",
    usage = "/plot relight",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.PLAYER)
public class Relight extends Command {
    public Relight() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            Captions.NOT_IN_PLOT.send(player);
            return CompletableFuture.completedFuture(false);
        }
        final LocalBlockQueue queue = plot.getArea().getQueue(false);
        ChunkManager.chunkTask(plot, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                queue.fixChunkLighting(value[0], value[1]);
            }
        }, () -> {
            plot.refreshChunks();
            Captions.SET_BLOCK_ACTION_FINISHED.send(player);
        }, 5);

        return CompletableFuture.completedFuture(true);
    }
}
