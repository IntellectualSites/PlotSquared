package com.intellectualcrafters.plot.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created 2015-02-11 for PlotSquared
 *
 */
public class Location implements Cloneable, Comparable<Location> {
    private int x, y, z;
    private float yaw, pitch;
    private String world;
    private boolean built;
    private Object o;
    
    @Override
    public Location clone() {
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public Location(final String world, final int x, final int y, final int z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        built = false;
        o = null;
    }
    
    public Location() {
        this("", 0, 0, 0, 0, 0);
    }
    
    public Location(final String world, final int x, final int y, final int z) {
        this(world, x, y, z, 0f, 0f);
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
        return (x * x) + (y * y) + (z * z);
    }
    
    public double getEuclideanDistance(final Location l2) {
        return Math.sqrt(getEuclideanDistanceSquared(l2));
    }
    
    public boolean isInSphere(final Location origin, final int radius) {
        return getEuclideanDistanceSquared(origin) < (radius * radius);
    }
    
    @Override
    public int hashCode() {
        int hash = 127;
        hash = (hash * 31) + x;
        hash = (hash * 31) + y;
        hash = (hash * 31) + z;
        hash = (int) ((hash * 31) + getYaw());
        hash = (int) ((hash * 31) + getPitch());
        return (hash * 31) + (world == null ? 127 : world.hashCode());
    }
    
    public boolean isInAABB(final Location min, final Location max) {
        return (x >= min.getX()) && (x <= max.getX()) && (y >= min.getY()) && (y <= max.getY()) && (z >= min.getX()) && (z < max.getZ());
    }
    
    public void lookTowards(final int x, final int y) {
        final double l = this.x - x;
        final double w = z - z;
        final double c = Math.sqrt((l * l) + (w * w));
        if (((Math.asin(w / c) / Math.PI) * 180) > 90) {
            setYaw((float) (180 - ((-Math.asin(l / c) / Math.PI) * 180)));
        } else {
            setYaw((float) ((-Math.asin(l / c) / Math.PI) * 180));
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
        return (x == l.getX()) && (y == l.getY()) && (z == l.getZ()) && world.equals(l.getWorld()) && (yaw == l.getY()) && (pitch == l.getPitch());
    }
    
    @Override
    public int compareTo(final Location o) {
        if (o == null) {
            throw new NullPointerException("Specified object was null");
        }
        if (((x == o.getX()) && (y == o.getY())) || (z == o.getZ())) {
            return 0;
        }
        if ((x < o.getX()) && (y < o.getY()) && (z < o.getZ())) {
            return -1;
        }
        return 1;
    }
    
    @Override
    public String toString() {
        return "\"plotsquaredlocation\":{" + "\"x\":" + x + ",\"y\":" + y + ",\"z\":" + z + ",\"yaw\":" + yaw + ",\"pitch\":" + pitch + ",\"world\":\"" + world + "\"}";
    }
    
    private Object getBukkitWorld() {
        try {
            final Class clazz = Class.forName("org.bukkit.Bukkit");
            return clazz.getMethod("getWorld", String.class).invoke(null, world);
        } catch (final Exception e) {
            return null;
        }
    }
    
    public Object toBukkitLocation() {
        if (built) {
            return o;
        }
        try {
            final Constructor constructor = Class.forName("org.bukkit.Location").getConstructor(Class.forName("org.bukkit.World"), double.class, double.class, double.class, float.class, float.class);
            built = true;
            return (o = constructor.newInstance(Class.forName("org.bukkit.World").cast(getBukkitWorld()), x, y, z, yaw, pitch));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            return null;
        }
    }
    
    /**
     * Please use utility class as this is not efficient
     */
    public void teleport(final Object o) throws Exception {
        if (o.getClass().getName().contains("org.bukkit.entity")) {
            final Method m = o.getClass().getMethod("teleport", Class.forName("org.bukkit.Location"));
            m.invoke(o, toBukkitLocation());
        }
    }
}
