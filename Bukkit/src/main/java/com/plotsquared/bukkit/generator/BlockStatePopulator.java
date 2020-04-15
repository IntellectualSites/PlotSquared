package com.plotsquared.bukkit.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.ChunkWrapper;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.queue.ScopedLocalBlockQueue;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

final class BlockStatePopulator extends BlockPopulator {

    private final IndependentPlotGenerator plotGenerator;
    private LocalBlockQueue queue;

    public BlockStatePopulator(IndependentPlotGenerator plotGenerator) {
        this.plotGenerator = plotGenerator;
    }

    @Override public void populate(@NotNull final World world, @NotNull final Random random,
        @NotNull final Chunk source) {
        if (this.queue == null) {
            this.queue = GlobalBlockQueue.IMP.getNewQueue(world.getName(), false);
        }
        final PlotArea area = PlotSquared.get().getPlotArea(world.getName(), null);
        final ChunkWrapper wrap = new ChunkWrapper(area.getWorldName(), source.getX(), source.getZ());
        final ScopedLocalBlockQueue chunk = this.queue.getForChunk(wrap.x, wrap.z);
        if (this.plotGenerator.populateChunk(chunk, area)) {
            this.queue.flush();
        }
    }

}
