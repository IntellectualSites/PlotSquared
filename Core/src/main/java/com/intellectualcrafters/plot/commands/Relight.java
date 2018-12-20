package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.block.LocalBlockQueue;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.HashSet;

@CommandDeclaration(command = "relight",
        description = "Relight your plot",
        usage = "/plot relight",
        category = CommandCategory.DEBUG)
public class Relight extends Command {
    public Relight() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public void execute(final PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            C.NOT_IN_PLOT.send(player);
            return;
        }
        HashSet<RegionWrapper> regions = plot.getRegions();
        final LocalBlockQueue queue = plot.getArea().getQueue(false);
        ChunkManager.chunkTask(plot, new RunnableVal<int[]>() {
            @Override
            public void run(int[] value) {
                queue.fixChunkLighting(value[0], value[1]);
            }
        }, new Runnable() {
            @Override
            public void run() {
                plot.refreshChunks();
                C.SET_BLOCK_ACTION_FINISHED.send(player);
            }
        }, 5);
    }
}
