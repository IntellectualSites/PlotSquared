package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.util.MathMan;

public class Location implements Cloneable, Comparable<Location> {

    private int x;
    private int y;
    private int z;
    private float yaw;
    private float pitch;
    private String world;

    public Location(String world, int x, int y, int z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location() {
        this.world = "";
    }

    public Location(String world, int x, int y, int z) {
        this(world, x, y, z, 0f, 0f);
    }

    @Override
    public Location clone() {
        return new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public PlotArea getPlotArea() {
        return PS.get().getPlotAreaAbs(this);
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

    public ChunkLoc getChunkLoc() {
        return new ChunkLoc(this.x >> 4, this.z >> 4);
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
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

    @Override
    public int hashCode() {
        return MathMan.pair((short) this.x, (short) this.z) * 17 + this.y;
    }

    public boolean isInAABB(Location min, Location max) {
        return this.x >= min.getX() && this.x <= max.getX() && this.y >= min.getY() && this.y <= max.getY() && this.z >= min.getX() && this.z < max
                .getZ();
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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        Location l = (Location) o;
        return this.x == l.getX() && this.y == l.getY() && this.z == l.getZ() && this.world.equals(l.getWorld()) && this.yaw == l.getYaw()
                && this.pitch == l.getPitch();
    }

    @Override
    public int compareTo(Location o) {
        if (this.x == o.getX() && this.y == o.getY() || this.z == o.getZ()) {
            return 0;
        }
        if (this.x < o.getX() && this.y < o.getY() && this.z < o.getZ()) {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "\"plotsquaredlocation\":{\"x\":" + this.x + ",\"y\":" + this.y + ",\"z\":" + this.z + ",\"yaw\":" + this.yaw + ",\"pitch\":"
                + this.pitch
                + ",\"world\":\"" + this.world + "\"}";
    }
}
