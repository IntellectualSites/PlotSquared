/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.util.RegionUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class PlotCluster {

    public PlotArea area;
    public PlotSettings settings;
    public UUID owner;
    public HashSet<UUID> helpers = new HashSet<>();
    public HashSet<UUID> invited = new HashSet<>();
    public int temp;
    private PlotId pos1;
    private PlotId pos2;
    private CuboidRegion region;

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
        this.region = RegionUtil.createRegion(this.pos1.getX(), this.pos2.getX(), 0, 0,
                this.pos1.getY(), this.pos2.getY()
        );
    }

    /**
     * Returns a region of PlotIDs
     *
     * @deprecated - returns region of IDs, not of actual blocks.
     */
    @Deprecated
    public CuboidRegion getRegion() {
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
     * @return area of plots
     */
    public int getArea() {
        return (1 + this.pos2.getX() - this.pos1.getX()) * (1 + this.pos2.getY() - this.pos1.getY());
    }

    public void setArea(PlotArea plotArea) {
        if (this.area != null) {
            this.area.removeCluster(this);
        }
        this.area = plotArea;
        plotArea.addCluster(this);
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
        PlotCluster other = (PlotCluster) obj;
        return this.pos1.equals(other.pos1) && this.pos2.equals(other.pos2) && this.area
                .equals(other.area);
    }

    @Override
    public String toString() {
        return this.area + ";" + this.pos1.toString() + ";" + this.pos2.toString();
    }

    public void getHome(final @NonNull Consumer<Location> result) {
        final BlockLoc home = this.settings.getPosition();
        Consumer<Location> locationConsumer = toReturn ->
                PlotSquared.platform().worldUtil().getHighestBlock(this.area.getWorldName(), toReturn.getX(), toReturn.getZ(),
                        highest -> {
                            if (highest <= area.getMinBuildHeight()) {
                                highest = 63;
                            }
                            if (highest > toReturn.getY()) {
                                result.accept(toReturn.withY(1 + highest));
                            } else {
                                result.accept(toReturn);
                            }
                        }
                );
        if (home.getY() == Integer.MIN_VALUE) {
            // default pos
            Plot center = getCenterPlot();
            center.getHome(location -> {
                Location toReturn = location;
                if (toReturn.getY() <= area.getMinBuildHeight()) {
                    PlotManager manager = this.area.getPlotManager();
                    Location locationSign = manager.getSignLoc(center);
                    toReturn = toReturn.withY(locationSign.getY());
                }
                locationConsumer.accept(toReturn);
            });
        } else {
            locationConsumer.accept(getClusterBottom().add(home.getX(), home.getY(), home.getZ()));
        }
    }

    public @NonNull PlotId getCenterPlotId() {
        final PlotId bot = getP1();
        final PlotId top = getP2();
        return PlotId.of((bot.getX() + top.getX()) / 2, (bot.getY() + top.getY()) / 2);
    }

    public @Nullable Plot getCenterPlot() {
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
        return pos1.getX() <= this.pos2.getX() && pos2.getX() >= this.pos1.getX() &&
                pos1.getY() <= this.pos2.getY() && pos2.getY() >= this.pos1.getY();
    }

    public boolean contains(PlotId id) {
        return this.pos1.getX() <= id.getX() && this.pos1.getY() <= id.getY() &&
                this.pos2.getX() >= id.getX() && this.pos2.getY() >= id.getY();
    }

}
