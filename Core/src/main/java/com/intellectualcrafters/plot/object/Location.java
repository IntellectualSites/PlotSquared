package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.util.MathMan;

/**
 * Created 2015-02-11 for PlotSquared
 *

 */
public class Location implements Cloneable, Comparable<Location> {
    private int x, y, z;
    private float yaw, pitch;
    private String world;
    private boolean built;

    public Location(final String world, final int x, final int y, final int z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        built = false;
    }
    
    public Location() {
        this("", 0, 0, 0, 0, 0);
    }
    
    public Location(final String world, final int x, final int y, final int z) {
        this(world, x, y, z, 0f, 0f);
    }

    @Override
    public Location clone() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public int getX() {
        return x;
    }
    
    public void setX(final int x) {
        this.x = x;
        built = false;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(final int y) {
        this.y = y;
        built = false;
    }
    
    public int getZ() {
        return z;
    }
    
    public void setZ(final int z) {
        this.z = z;
        built = false;
    }
    
    public String getWorld() {
        return world;
    }

    public void setWorld(final String world) {
        this.world = world;
        built = false;
    }

    public PlotArea getPlotArea() {
        return PS.get().getPlotAreaAbs(this);
    }
    
    public Plot getOwnedPlot() {
        PlotArea area = PS.get().getPlotAreaAbs(this);
        return area != null ? area.getOwnedPlot(this) : null;
    }

    public Plot getOwnedPlotAbs() {
        PlotArea area = PS.get().getPlotAreaAbs(this);
        return area != null ? area.getOwnedPlotAbs(this) : null;
    }

    public boolean isPlotArea() {
        return PS.get().getPlotAreaAbs(this) != null;
    }
    
    public boolean isPlotRoad() {
        PlotArea area = PS.get().getPlotAreaAbs(this);
        return area != null && area.getPlotAbs(this) == null;
    }

    public boolean isUnownedPlotArea() {
        PlotArea area = PS.get().getPlotAreaAbs(this);
        return area != null && area.getOwnedPlotAbs(this) == null;
    }

    public PlotManager getPlotManager() {
        PlotArea pa = getPlotArea();
        return pa != null ? pa.getPlotManager() : null;
    }

    public Plot getPlotAbs() {
        PlotArea area = PS.get().getPlotAreaAbs(this);
        return area != null ? area.getPlotAbs(this) : null;
    }

    public Plot getPlot() {
        PlotArea area = PS.get().getPlotAreaAbs(this);
        return area != null ? area.getPlot(this) : null;
    }

    public ChunkLoc getChunkLoc() {
        return new ChunkLoc(x >> 4, z >> 4);
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public void setYaw(final float yaw) {
        this.yaw = yaw;
        built = false;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public void setPitch(final float pitch) {
        this.pitch = pitch;
        built = false;
    }
    
    public Location add(final int x, final int y, final int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        built = false;
        return this;
    }
    
    public double getEuclideanDistanceSquared(final Location l2) {
        final double x = getX() - l2.getX();
        final double y = getY() - l2.getY();
        final double z = getZ() - l2.getZ();
        return x * x + y * y + z * z;
    }
    
    public double getEuclideanDistance(final Location l2) {
        return Math.sqrt(getEuclideanDistanceSquared(l2));
    }
    
    public boolean isInSphere(final Location origin, final int radius) {
        return getEuclideanDistanceSquared(origin) < radius * radius;
    }
    
    @Override
    public int hashCode() {
        return MathMan.pair((short) x, (short) z) * 17 + y;
    }
    
    public boolean isInAABB(final Location min, final Location max) {
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getX() && z < max.getZ();
    }
    
    public void lookTowards(final int x, final int y) {
        final double l = this.x - x;
        final double c = Math.sqrt(l * l + 0.0);
        if (Math.asin(0 / c) / Math.PI * 180 > 90) {
            setYaw((float) (180 - -Math.asin(l / c) / Math.PI * 180));
        } else {
            setYaw((float) (-Math.asin(l / c) / Math.PI * 180));
        }
        built = false;
    }
    
    public Location subtract(final int x, final int y, final int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        built = false;
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        final Location l = (Location) o;
        return x == l.getX() && y == l.getY() && z == l.getZ() && world.equals(l.getWorld()) && yaw == l.getY() && pitch == l.getPitch();
    }
    
    @Override
    public int compareTo(final Location o) {
        if (o == null) {
            throw new NullPointerException("Specified object was null");
        }
        if (x == o.getX() && y == o.getY() || z == o.getZ()) {
            return 0;
        }
        if (x < o.getX() && y < o.getY() && z < o.getZ()) {
            return -1;
        }
        return 1;
    }
    
    @Override
    public String toString() {
        return "\"plotsquaredlocation\":{" + "\"x\":" + x + ",\"y\":" + y + ",\"z\":" + z + ",\"yaw\":" + yaw + ",\"pitch\":" + pitch + ",\"world\":\"" + world + "\"}";
    }
}
