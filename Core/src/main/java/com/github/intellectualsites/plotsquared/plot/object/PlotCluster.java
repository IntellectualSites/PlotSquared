package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

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

    public PlotCluster(PlotArea area, PlotId pos1, PlotId pos2, UUID owner) {
        this.area = area;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        this.settings = new PlotSettings();
        this.temp = -1;
        setRegion();
    }

    public PlotCluster(PlotArea area, PlotId pos1, PlotId pos2, UUID owner, int temp) {
        this.area = area;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.owner = owner;
        this.settings = new PlotSettings();
        this.temp = temp;
        setRegion();
    }

    public PlotId getP1() {
        return this.pos1;
    }

    public void setP1(PlotId id) {
        this.pos1 = id;
        setRegion();
    }

    public PlotId getP2() {
        return this.pos2;
    }

    public void setP2(PlotId id) {
        this.pos2 = id;
        setRegion();
    }

    private void setRegion() {
        this.region = new RegionWrapper(this.pos1.x, this.pos2.x, this.pos1.y, this.pos2.y);
    }

    public RegionWrapper getRegion() {
        return this.region;
    }

    public boolean isOwner(UUID uuid) {
        return uuid.equals(owner);
    }

    public boolean isAdded(UUID uuid) {
        return this.owner.equals(uuid) || this.invited.contains(uuid) || this.invited
            .contains(DBFunc.EVERYONE) || this.helpers.contains(uuid) || this.helpers
            .contains(DBFunc.EVERYONE);
    }

    public boolean hasHelperRights(UUID uuid) {
        return this.owner.equals(uuid) || this.helpers.contains(uuid) || this.helpers
            .contains(DBFunc.EVERYONE);
    }

    public String getName() {
        return this.settings.getAlias();
    }

    /**
     * Get the area (in plots).
     *
     * @return
     */
    public int getArea() {
        return (1 + this.pos2.x - this.pos1.x) * (1 + this.pos2.y - this.pos1.y);
    }

    public void setArea(PlotArea plotArea) {
        if (this.area != null) {
            this.area.removeCluster(this);
        }
        this.area = plotArea;
        plotArea.addCluster(this);
    }

    @Override public int hashCode() {
        return this.pos1.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlotCluster other = (PlotCluster) obj;
        return this.pos1.equals(other.pos1) && this.pos2.equals(other.pos2) && this.area
            .equals(other.area);
    }

    @Override public String toString() {
        return this.area + ";" + this.pos1.x + ";" + this.pos1.y + ";" + this.pos2.x + ";"
            + this.pos2.y;
    }

    public Location getHome() {
        BlockLoc home = this.settings.getPosition();
        Location toReturn;
        if (home.y == 0) {
            // default pos
            Plot center = getCenterPlot();
            toReturn = center.getHome();
            if (toReturn.getY() == 0) {
                PlotManager manager = this.area.getPlotManager();
                Location location = manager.getSignLoc(center);
                toReturn.setY(location.getY());
            }
        } else {
            toReturn = getClusterBottom().add(home.x, home.y, home.z);
        }
        int max = MainUtil.getHeighestBlock(this.area.worldname, toReturn.getX(), toReturn.getZ());
        if (max > toReturn.getY()) {
            toReturn.setY(1 + max);
        }
        return toReturn;
    }

    public PlotId getCenterPlotId() {
        PlotId bot = getP1();
        PlotId top = getP2();
        return new PlotId((bot.x + top.x) / 2, (bot.y + top.y) / 2);
    }

    public Plot getCenterPlot() {
        return this.area.getPlotAbs(getCenterPlotId());
    }

    public Location getClusterBottom() {
        PlotManager manager = this.area.getPlotManager();
        return manager.getPlotBottomLocAbs(getP1());
    }

    public Location getClusterTop() {
        PlotManager manager = this.area.getPlotManager();
        return manager.getPlotTopLocAbs(getP2());
    }

    public boolean intersects(PlotId pos1, PlotId pos2) {
        return pos1.x <= this.pos2.x && pos2.x >= this.pos1.x && pos1.y <= this.pos2.y
            && pos2.y >= this.pos1.y;
    }

    public boolean contains(PlotId id) {
        return this.pos1.x <= id.x && this.pos1.y <= id.y && this.pos2.x >= id.x
            && this.pos2.y >= id.y;
    }
}
