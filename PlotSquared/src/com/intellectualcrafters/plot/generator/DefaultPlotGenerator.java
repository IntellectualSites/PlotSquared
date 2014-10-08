package com.intellectualcrafters.plot.generator;

import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PlotGenerator;
import com.intellectualcrafters.plot.PlotManager;
import com.intellectualcrafters.plot.PlotWorld;

public class DefaultPlotGenerator extends PlotGenerator {

    private final PlotWorld plotworld;
    private final ChunkGenerator generator;
    private static PlotManager manager = null;
    
    public DefaultPlotGenerator(String worldname) {
        super(worldname);
        
        this.plotworld = new DefaultPlotWorld(worldname);
        this.generator = new WorldGenerator(plotworld, worldname);
        if (manager==null) {
            DefaultPlotGenerator.manager = new DefaultPlotManager();
        }
    }
    
    @Override
    public PlotWorld getPlotWorld(String worldname) {
        return this.plotworld;
    }
    
    @Override
    public ChunkGenerator getChunkGenerator(PlotWorld plotworld, String worldname) {
        return this.generator;
    }

    @Override
    public PlotManager getPlotManager(PlotWorld plotworld) {
        return manager;
    }
}
