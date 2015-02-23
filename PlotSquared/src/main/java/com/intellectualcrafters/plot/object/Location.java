package com.intellectualcrafters.plot.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created 2015-02-11 for PlotSquared
 *
 * @author Citymonstret
 */
public class Location implements Cloneable, Comparable<Location> {
    private int x, y, z;
    private float yaw, pitch;
    private String world;
    private boolean built;
    private Object o;

    @Override
    public Location clone() {
        return new Location(this.world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public Location(final String world, final int x, final int y, final int z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.built = false;
        this.o = null;
    }

    public Location() {
        this("", 0, 0, 0, 0, 0);
    }

    public Location(final String world, final int x, final int y, final int z) {
        this(world, x, y, z, 0f, 0f);
    }

    public int getX() {
        return this.x;
    }

    public void setX(final int x) {
        this.x = x;
        this.built = false;
    }

    public int getY() {
        return this.y;
    }

    public void setY(final int y) {
        this.y = y;
        this.built = false;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(final int z) {
        this.z = z;
        this.built = false;
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(final String world) {
        this.world = world;
        this.built = false;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
        this.built = false;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
        this.built = false;
    }

    public Location add(final int x, final int y, final int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.built = false;
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
        hash = (hash * 31) + this.x;
        hash = (hash * 31) + this.y;
        hash = (hash * 31) + this.z;
        hash = (int) ((hash * 31) + getYaw());
        hash = (int) ((hash * 31) + getPitch());
        return (hash * 31) + (this.world == null ? 127 : this.world.hashCode());
    }

    public boolean isInAABB(final Location min, final Location max) {
        return (this.x >= min.getX()) && (this.x <= max.getX()) && (this.y >= min.getY()) && (this.y <= max.getY()) && (this.z >= min.getX()) && (this.z < max.getZ());
    }

    public void lookTowards(final int x, final int y) {
        final double l = this.x - x;
        final double w = this.z - this.z;
        final double c = Math.sqrt((l * l) + (w * w));
        if (((Math.asin(w / c) / Math.PI) * 180) > 90) {
            setYaw((float) (180 - ((-Math.asin(l / c) / Math.PI) * 180)));
        } else {
            setYaw((float) ((-Math.asin(l / c) / Math.PI) * 180));
        }
        this.built = false;
    }

    public Location subtract(final int x, final int y, final int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.built = false;
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
        return (this.x == l.getX()) && (this.y == l.getY()) && (this.z == l.getZ()) && this.world.equals(l.getWorld()) && (this.yaw == l.getY()) && (this.pitch == l.getPitch());
    }

    @Override
    public int compareTo(final Location o) {
        if (o == null) {
            throw new NullPointerException("Specified object was null");
        }
        if (((this.x == o.getX()) && (this.y == o.getY())) || (this.z == o.getZ())) {
            return 0;
        }
        if ((this.x < o.getX()) && (this.y < o.getY()) && (this.z < o.getZ())) {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "\"plotsquaredlocation\":{" + "\"x\":" + this.x + ",\"y\":" + this.y + ",\"z\":" + this.z + ",\"yaw\":" + this.yaw + ",\"pitch\":" + this.pitch + ",\"world\":\"" + this.world + "\"}";
    }

    private Object getBukkitWorld() {
        try {
            final Class clazz = Class.forName("org.bukkit.Bukkit");
            return clazz.getMethod("getWorld", String.class).invoke(null, this.world);
        } catch (final Exception e) {
            return null;
        }
    }

    public Object toBukkitLocation() {
        if (this.built) {
            return this.o;
        }
        try {
            final Constructor constructor = Class.forName("org.bukkit.Location").getConstructor(Class.forName("org.bukkit.World"), double.class, double.class, double.class, float.class, float.class);
            this.built = true;
            return (this.o = constructor.newInstance(Class.forName("org.bukkit.World").cast(getBukkitWorld()), this.x, this.y, this.z, this.yaw, this.pitch));
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
