package com.intellectualcrafters.plot;

import org.bukkit.generator.ChunkGenerator;

public abstract class PlotGenerator extends ChunkGenerator {

	public PlotGenerator(String world) {
		PlotMain.loadWorld(world, this);
		System.out.print("LOADED");
	}

	public abstract PlotWorld getNewPlotWorld(String world);

	public abstract PlotManager getPlotManager();
}
