package com.plotsquared.bukkit.util.block;

import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import org.bukkit.Chunk;

public class FastQueue_1_9 extends SlowQueue {

    @Override public PlotChunk<Chunk> getChunk(SetQueue.ChunkWrapper wrap) {
        return new FastChunk_1_9(wrap);
    }
}
