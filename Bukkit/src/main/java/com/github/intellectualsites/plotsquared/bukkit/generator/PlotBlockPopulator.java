package com.github.intellectualsites.plotsquared.bukkit.generator;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.ChunkWrapper;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@RequiredArgsConstructor final class BlockStatePopulator extends BlockPopulator {

    private final IndependentPlotGenerator plotGenerator;
    private LocalBlockQueue queue;

    @Override public void populate(@NotNull final World world, @NotNull final Random random,
        @NotNull final Chunk source) {
        if (this.queue == null) {
            this.queue = GlobalBlockQueue.IMP.getNewQueue(world.getName(), false);
        }
        final PlotArea area = PlotSquared.get().getPlotArea(world.getName(), null);
        final ChunkWrapper wrap = new ChunkWrapper(area.worldname, source.getX(), source.getZ());
        final ScopedLocalBlockQueue chunk = this.queue.getForChunk(wrap.x, wrap.z);
        if (this.plotGenerator.populateChunk(chunk, area)) {
            this.queue.flush();
        }
    }

}
