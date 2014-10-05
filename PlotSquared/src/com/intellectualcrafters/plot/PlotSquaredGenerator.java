package com.intellectualcrafters.plot;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public interface PlotSquaredGenerator {
    public ChunkGenerator getChunkGenerator();
    
    public BlockPopulator getBlockPopulator();
    
    public PlotManager getPlotManager();
    
    public PlotWorld getPlotWorld();
}
