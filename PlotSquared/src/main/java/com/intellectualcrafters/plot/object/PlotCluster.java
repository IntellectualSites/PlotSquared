package com.intellectualcrafters.plot.object;

import java.util.HashSet;
import java.util.UUID;

public class PlotCluster {

	public final String world;
	
	public PlotSettings settings;
	public UUID owner;

	public HashSet<UUID> helpers = new HashSet<UUID>();
	
	private PlotId pos1;
	private PlotId pos2;
	
	public PlotId getP1() {
		return this.pos1;
	}
	
	public PlotId getP2() {
		return this.pos2;
	}
	
	public void setP1(PlotId id) {
		this.pos1 = id;
	}
	
	public void setP2(PlotId id) {
		this.pos2 = id;
	}
	
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
