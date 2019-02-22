package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;

import java.util.HashSet;

@CommandDeclaration(command = "relight", description = "Relight your plot", usage = "/plot relight",
    category = CommandCategory.DEBUG) public class Relight extends Command {
    public Relight() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            Captions.NOT_IN_PLOT.send(player);
            return;
        }
        HashSet<RegionWrapper> regions = plot.getRegions();
        final LocalBlockQueue queue = plot.getArea().getQueue(false);
        ChunkManager.chunkTask(plot, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                queue.fixChunkLighting(value[0], value[1]);
            }
        }, new Runnable() {
            @Override public void run() {
                plot.refreshChunks();
                Captions.SET_BLOCK_ACTION_FINISHED.send(player);
            }
        }, 5);
    }
}
