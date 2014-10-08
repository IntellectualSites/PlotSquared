package com.intellectualcrafters.plot;

import org.bukkit.generator.ChunkGenerator;

public abstract class PlotGenerator extends ChunkGenerator {
    
    public abstract PlotWorld getPlotWorld();
    
    public abstract PlotManager getPlotManager();
    
    
}
