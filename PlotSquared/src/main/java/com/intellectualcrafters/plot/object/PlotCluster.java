package com.intellectualcrafters.plot.object;

import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.SQLManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class PlotCluster {

	public final String world;
	
	public PlotSettings settings;
	public UUID owner;

	public HashSet<UUID> helpers = new HashSet<UUID>();
	public HashSet<UUID> invited = new HashSet<UUID>();
	
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
		this.settings = new PlotSettings(null);
	}
	
	public boolean hasRights(UUID uuid) {
	    return (invited.contains(uuid)|| invited.contains(DBFunc.everyone) || helpers.contains(uuid) || helpers.contains(DBFunc.everyone));
	}
	
	public String getName() {
	    return this.settings.getAlias();
	}
	
	@Override
	public int hashCode() {
		return this.pos1.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlotCluster other = (PlotCluster) obj;
        return (this.world.equals(other.world) && this.pos1.equals(other.pos1) && this.pos2.equals(other.pos2));
	}
	
	@Override
	public String toString() {
		return world + ";" + pos1.x + ";" + pos1.y + ";" + pos2.x + ";" + pos2.y;
	}
}
