package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.MainUtil;

import java.util.HashSet;
import java.util.UUID;

public class PlotCluster {
    public PlotArea area;
    public PlotSettings settings;
    public UUID owner;
    public HashSet<UUID> helpers = new HashSet<>();
    public HashSet<UUID> invited = new HashSet<>();
    public int temp;
    private PlotId pos1;
    private PlotId pos2;
    private RegionWrapper region;

    public PlotCluster(final PlotArea area, final PlotId pos1, final PlotId pos2, final UUID owner) {
        this.area = area;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        settings = new PlotSettings();
        this.temp = -1;
        setRegion();
    }
    
    public PlotCluster(final PlotArea area, final PlotId pos1, final PlotId pos2, final UUID owner, int temp) {
        this.area = area;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        settings = new PlotSettings();
        this.temp = temp;
        setRegion();
    }

    public PlotId getP1() {
        return pos1;
    }

    public void setP1(final PlotId id) {
        pos1 = id;
        setRegion();
    }

    public PlotId getP2() {
        return pos2;
    }

    public void setP2(final PlotId id) {
        pos2 = id;
        setRegion();
    }
    
    private void setRegion() {
        region = new RegionWrapper(pos1.x, pos2.x, pos1.y, pos2.y);
    }
    
    public RegionWrapper getRegion() {
        return region;
    }

    public boolean isAdded(final UUID uuid) {
        return owner.equals(uuid) || invited.contains(uuid) || invited.contains(DBFunc.everyone) || helpers.contains(uuid) || helpers
                .contains(DBFunc.everyone);
    }
    
    public boolean hasHelperRights(final UUID uuid) {
        return owner.equals(uuid) || helpers.contains(uuid) || helpers.contains(DBFunc.everyone);
    }
    
    public String getName() {
        return settings.getAlias();
    }
    
    /**
     * Get the area (in plots)
     * @return
     */
    public int getArea() {
        return (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
    }

    public void setArea(PlotArea plotarea) {
        if (this.area != null) {
            this.area.removeCluster(this);
        }
        this.area = plotarea;
        plotarea.addCluster(this);
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
        return pos1.equals(other.pos1) && pos2.equals(other.pos2) && area.equals(other.area);
    }
    
    @Override
    public String toString() {
        return area + ";" + pos1.x + ";" + pos1.y + ";" + pos2.x + ";" + pos2.y;
    }
    
    public Location getHome() {
        final BlockLoc home = settings.getPosition();
        Location toReturn;
        if (home.y == 0) {
            // default pos
            final Plot center = getCenterPlot();
            toReturn = center.getHome();
            if (toReturn.getY() == 0) {
                final PlotManager manager = area.getPlotManager();
                final Location loc = manager.getSignLoc(area, center);
                toReturn.setY(loc.getY());
            }
        } else {
            toReturn = getClusterBottom().add(home.x, home.y, home.z);
        }
        final int max = MainUtil.getHeighestBlock(area.worldname, toReturn.getX(), toReturn.getZ());
        if (max > toReturn.getY()) {
            toReturn.setY(max);
        }
        return toReturn;
    }
    
    public PlotId getCenterPlotId() {
        final PlotId bot = getP1();
        final PlotId top = getP2();
        return new PlotId((bot.x + top.x) / 2, (bot.y + top.y) / 2);
    }

    public Plot getCenterPlot() {
        return area.getPlotAbs(getCenterPlotId());
    }

    public Location getClusterBottom() {
        final PlotManager manager = area.getPlotManager();
        return manager.getPlotBottomLocAbs(area, getP1());
    }
    
    public Location getClusterTop() {
        final PlotManager manager = area.getPlotManager();
        return manager.getPlotTopLocAbs(area, getP2());
    }
    
    public boolean intersects(PlotId pos1, PlotId pos2) {
        return pos1.x <= this.pos2.x && pos2.x >= this.pos1.x && pos1.y <= this.pos2.y && pos2.y >= this.pos1.y;
    }
    
    public boolean contains(final PlotId id) {
        return pos1.x <= id.x && pos1.y <= id.y && pos2.x >= id.x && pos2.y >= id.y;
    }
}
