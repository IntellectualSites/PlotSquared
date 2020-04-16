/*
 *
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.location;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import lombok.Getter;
import lombok.Setter;

public class Location implements Cloneable, Comparable<Location> {

    private int x;
    private int y;
    private int z;
    @Getter @Setter private float yaw;
    @Getter @Setter private float pitch;
    @Getter @Setter private String world;
    @Getter private BlockVector3 blockVector3;

    public Location(String world, int x, int y, int z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public Location(String world, int x, int y, int z) {
        this(world, x, y, z, 0f, 0f);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
        this.blockVector3 = BlockVector3.at(x, y, z);
    }

    public void setBlockVector3(BlockVector3 blockVector3) {
        this.blockVector3 = blockVector3;
        this.x = blockVector3.getX();
        this.y = blockVector3.getY();
        this.z = blockVector3.getZ();
    }

    @Override public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); //can't happen
        }
    }

    public PlotArea getPlotArea() {
        return PlotSquared.get().getPlotAreaAbs(this);
    }

    public Plot getOwnedPlot() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getOwnedPlot(this);
        } else {
            return null;
        }
    }

    public Plot getOwnedPlotAbs() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getOwnedPlotAbs(this);
        } else {
            return null;
        }
    }

    public boolean isPlotArea() {
        return getPlotArea() != null;
    }

    public boolean isPlotRoad() {
        PlotArea area = getPlotArea();
        return area != null && area.getPlotAbs(this) == null;
    }

    /**
     * Checks if anyone owns a plot at the current location.
     *
     * @return true if the location is a road, not a plot area, or if the plot is unclaimed.
     */
    public boolean isUnownedPlotArea() {
        PlotArea area = getPlotArea();
        return area != null && area.getOwnedPlotAbs(this) == null;
    }

    public PlotManager getPlotManager() {
        PlotArea pa = getPlotArea();
        if (pa != null) {
            return pa.getPlotManager();
        } else {
            return null;
        }
    }

    public Plot getPlotAbs() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getPlotAbs(this);
        } else {
            return null;
        }
    }

    public Plot getPlot() {
        PlotArea area = getPlotArea();
        if (area != null) {
            return area.getPlot(this);
        } else {
            return null;
        }
    }

    public BlockVector2 getBlockVector2() {
        return BlockVector2.at(this.x >> 4, this.z >> 4);
    }

    public Location add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public double getEuclideanDistanceSquared(Location l2) {
        double x = getX() - l2.getX();
        double y = getY() - l2.getY();
        double z = getZ() - l2.getZ();
        return x * x + y * y + z * z;
    }

    public double getEuclideanDistance(Location l2) {
        return Math.sqrt(getEuclideanDistanceSquared(l2));
    }

    public boolean isInSphere(Location origin, int radius) {
        return getEuclideanDistanceSquared(origin) < radius * radius;
    }

    @Override public int hashCode() {
        return MathMan.pair((short) this.x, (short) this.z) * 17 + this.y;
    }

    public boolean isInAABB(Location min, Location max) {
        return this.x >= min.getX() && this.x <= max.getX() && this.y >= min.getY() && this.y <= max
            .getY() && this.z >= min.getX() && this.z < max.getZ();
    }

    public void lookTowards(int x, int y) {
        double l = this.x - x;
        double c = Math.sqrt(l * l + 0.0);
        if (Math.asin(0 / c) / Math.PI * 180 > 90) {
            setYaw((float) (180 - -Math.asin(l / c) / Math.PI * 180));
        } else {
            setYaw((float) (-Math.asin(l / c) / Math.PI * 180));
        }
    }

    public Location subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    @Override public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        Location l = (Location) o;
        return this.x == l.getX() && this.y == l.getY() && this.z == l.getZ() && this.world
            .equals(l.getWorld()) && this.yaw == l.getYaw() && this.pitch == l.getPitch();
    }

    @Override public int compareTo(Location o) {
        if (this.x == o.getX() && this.y == o.getY() || this.z == o.getZ()) {
            return 0;
        }
        if (this.x < o.getX() && this.y < o.getY() && this.z < o.getZ()) {
            return -1;
        }
        return 1;
    }

    @Override public String toString() {
        return "\"plotsquaredlocation\":{\"x\":" + this.x + ",\"y\":" + this.y + ",\"z\":" + this.z
            + ",\"yaw\":" + this.yaw + ",\"pitch\":" + this.pitch + ",\"world\":\"" + this.world
            + "\"}";
    }
}
