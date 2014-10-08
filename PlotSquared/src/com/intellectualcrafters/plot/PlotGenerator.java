package com.intellectualcrafters.plot;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public abstract class PlotGenerator {
    
    public PlotGenerator(String world){
    }
    
    public abstract PlotWorld getPlotWorld(String world);
    
    public abstract ChunkGenerator getChunkGenerator(PlotWorld plotworld, String world);

    public abstract PlotManager getPlotManager(PlotWorld plotworld);
}
