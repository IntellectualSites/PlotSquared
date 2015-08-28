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
        return this.pos1;
    }

    public PlotId getP2() {
        return this.pos2;
    }

    public void setP1(final PlotId id) {
        this.pos1 = id;
    }

    public void setP2(final PlotId id) {
        this.pos2 = id;
    }
    
    public PlotCluster(final String world, final PlotId pos1, final PlotId pos2, final UUID owner) {
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        this.settings = new PlotSettings(null);
    }

    public boolean isAdded(final UUID uuid) {
        return (this.owner.equals(uuid) || this.invited.contains(uuid) || this.invited.contains(DBFunc.everyone) || this.helpers.contains(uuid) || this.helpers.contains(DBFunc.everyone));
    }

    public boolean hasHelperRights(final UUID uuid) {
        return (this.owner.equals(uuid) || this.helpers.contains(uuid) || this.helpers.contains(DBFunc.everyone));
    }

    public String getName() {
        return this.settings.getAlias();
    }
    
    /**
     * Get the area (in plots)
     * @return
     */
    public int getArea() {
        return (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
    }

    @Override
    public int hashCode() {
        return this.pos1.hashCode();
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
        return (this.world.equals(other.world) && this.pos1.equals(other.pos1) && this.pos2.equals(other.pos2));
    }

    @Override
    public String toString() {
        return this.world + ";" + this.pos1.x + ";" + this.pos1.y + ";" + this.pos2.x + ";" + this.pos2.y;
    }
}
