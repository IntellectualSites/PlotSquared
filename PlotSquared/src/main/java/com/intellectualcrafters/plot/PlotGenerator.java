package com.intellectualcrafters.plot;

import org.bukkit.generator.ChunkGenerator;

public abstract class PlotGenerator extends ChunkGenerator {

    public PlotGenerator(final String world) {
        PlotMain.loadWorld(world, this);
    }

    public abstract PlotWorld getNewPlotWorld(final String world);

    public abstract PlotManager getPlotManager();
}
