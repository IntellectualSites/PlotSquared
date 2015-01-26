package com.intellectualcrafters.plot.object;

import java.util.UUID;

public class PlotCluster {

	public PlotSettings settings;
	public final String world;
	public final PlotId pos1;
	public final PlotId pos2;
	public final UUID owner;
	
	public PlotCluster(String world, PlotId pos1, PlotId pos2, UUID owner) {
		this.world = world;
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.owner = owner;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return world + ";" + pos1.x + ";" + pos1.y + ";" + pos2.x + ";" + pos2.y;
	}
}
