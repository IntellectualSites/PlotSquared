package com.intellectualcrafters.plot.object;

import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.database.DBFunc;

public class PlotCluster {
    public final String world;
    public PlotSettings settings;
    public UUID owner;
    public HashSet<UUID> helpers = new HashSet<UUID>();
    public HashSet<UUID> invited = new HashSet<UUID>();
    private PlotId pos1;
    private PlotId pos2;
    
    public PlotId getP1() {
        return pos1;
    }
    
    public PlotId getP2() {
        return pos2;
    }
    
    public void setP1(final PlotId id) {
        pos1 = id;
    }
    
    public void setP2(final PlotId id) {
        pos2 = id;
    }
    
    public PlotCluster(final String world, final PlotId pos1, final PlotId pos2, final UUID owner) {
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        settings = new PlotSettings();
    }
    
    public boolean isAdded(final UUID uuid) {
        return (owner.equals(uuid) || invited.contains(uuid) || invited.contains(DBFunc.everyone) || helpers.contains(uuid) || helpers.contains(DBFunc.everyone));
    }
    
    public boolean hasHelperRights(final UUID uuid) {
        return (owner.equals(uuid) || helpers.contains(uuid) || helpers.contains(DBFunc.everyone));
    }
    
    public String getName() {
        return settings.getAlias();
    }
    
    /**
     * Get the area (in plots)
     * @return
     */
    public int getArea() {
        return ((1 + pos2.x) - pos1.x) * ((1 + pos2.y) - pos1.y);
    }
    
    @Override
    public int hashCode() {
        return pos1.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
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
        return (world.equals(other.world) && pos1.equals(other.pos1) && pos2.equals(other.pos2));
    }
    
    @Override
    public String toString() {
        return world + ";" + pos1.x + ";" + pos1.y + ";" + pos2.x + ";" + pos2.y;
    }
}
