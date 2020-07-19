/*
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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.SimpleMBR;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An unmodifiable 6-tuple (world,x,y,z,yaw,pitch)
 */
@SuppressWarnings("unused")
public final class Location extends BlockLoc implements Comparable<Location> {

    private final float yaw;
    private final float pitch;
    private final BlockVector3 blockVector3;
    private final World<?> world;

    private Location(@Nonnull final World<?> world, @Nonnull final BlockVector3 blockVector3,
        final float yaw, final float pitch) {
        super(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ(), yaw, pitch);
        this.world = Preconditions.checkNotNull(world, "World may not be null");
        this.blockVector3 = Preconditions.checkNotNull(blockVector3, "Vector may not be null");
        this.yaw = yaw;
        this.pitch = pitch;
    }

    private Location(@Nonnull final String worldName, @Nonnull final BlockVector3 blockVector3,
        final float yaw, final float pitch) {
        super(blockVector3.getX(), blockVector3.getY(), blockVector3.getZ(), yaw, pitch);
        Preconditions.checkNotNull(worldName, "World name may not be null");
        if (worldName.isEmpty()) {
            this.world = World.nullWorld();
        } else {
            this.world = PlotSquared.platform().getPlatformWorld(worldName);
        }
        this.blockVector3 = Preconditions.checkNotNull(blockVector3, "Vector may not be null");
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Construct a new location
     *
     * @param world        World
     * @param blockVector3 (x,y,z) vector
     * @param yaw          yaw
     * @param pitch        pitch
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final String world,
        @Nonnull final BlockVector3 blockVector3, final float yaw, final float pitch) {
        return new Location(world, blockVector3, yaw, pitch);
    }

    /**
     * Construct a new location with yaw and pitch equal to 0
     *
     * @param world        World
     * @param blockVector3 (x,y,z) vector
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final String world,
        @Nonnull final BlockVector3 blockVector3) {
        return at(world, blockVector3, 0f, 0f);
    }

    /**
     * Construct a new location
     *
     * @param world World
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @param yaw   Yaw
     * @param pitch Pitch
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final String world, final int x, final int y,
        final int z, final float yaw, final float pitch) {
        return at(world, BlockVector3.at(x, y, z), yaw, pitch);
    }

    /**
     * Construct a new location with yaw and pitch equal to 0
     *
     * @param world World
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final String world, final int x, final int y,
        final int z) {
        return at(world, BlockVector3.at(x, y, z));
    }

    /**
     * Construct a new location
     *
     * @param world        World
     * @param blockVector3 (x,y,z) vector
     * @param yaw          yaw
     * @param pitch        pitch
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final World<?> world,
        @Nonnull final BlockVector3 blockVector3, final float yaw, final float pitch) {
        return new Location(world, blockVector3, yaw, pitch);
    }

    /**
     * Construct a new location with yaw and pitch equal to 0
     *
     * @param world        World
     * @param blockVector3 (x,y,z) vector
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final World<?> world,
        @Nonnull final BlockVector3 blockVector3) {
        return at(world, blockVector3, 0f, 0f);
    }

    /**
     * Construct a new location
     *
     * @param world World
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @param yaw   Yaw
     * @param pitch Pitch
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final World<?> world, final int x, final int y,
        final int z, final float yaw, final float pitch) {
        return at(world, BlockVector3.at(x, y, z), yaw, pitch);
    }

    /**
     * Construct a new location with yaw and pitch equal to 0
     *
     * @param world World
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @return New location
     */
    @Nonnull public static Location at(@Nonnull final World<?> world, final int x, final int y,
        final int z) {
        return at(world, BlockVector3.at(x, y, z));
    }

    /**
     * Get the world object
     *
     * @return World object
     */
    @Nonnull public World<?> getWorld() {
        return this.world;
    }

    /**
     * Get the name of the world this location is in
     *
     * @return World name
     */
    @Nonnull public String getWorldName() {
        return this.world.getName();
    }

    /**
     * Get the X coordinate
     *
     * @return X coordinate
     */
    public int getX() {
        return this.blockVector3.getBlockX();
    }

    /**
     * Get the Y coordinate
     *
     * @return Y coordinate
     */
    public int getY() {
        return this.blockVector3.getY();
    }

    /**
     * Get the Z coordinate
     *
     * @return Z coordinate
     */
    public int getZ() {
        return this.blockVector3.getZ();
    }

    /**
     * Get the {@link PlotArea}, if any, that contains this location
     *
     * @return Plot area containing the location, or {@code null}
     */
    @Nullable public PlotArea getPlotArea() {
        return PlotSquared.get().getPlotAreaManager().getPlotArea(this);
    }

    /**
     * Get the owned {@link Plot}, if any, that contains this location
     *
     * @return Plot containing the location, or {@code null}
     */
    @Nullable public Plot getOwnedPlot() {
        final PlotArea area = this.getPlotArea();
        if (area != null) {
            return area.getOwnedPlot(this);
        } else {
            return null;
        }
    }

    /**
     * Get the (absolute) owned {@link Plot}, if any, that contains this location
     *
     * @return (Absolute) plot containing the location, or {@code null}
     */
    @Nullable public Plot getOwnedPlotAbs() {
        final PlotArea area = this.getPlotArea();
        if (area != null) {
            return area.getOwnedPlotAbs(this);
        } else {
            return null;
        }
    }

    /**
     * Check whether or not the location belongs to a plot area
     *
     * @return {@code true} if the location belongs to a plot area, else {@code false}
     */
    public boolean isPlotArea() {
        return this.getPlotArea() != null;
    }

    /**
     * Check whether or not the location belongs to a plot road
     *
     * @return {@code true} if the location belongs to a plot road, else {@code false}
     */
    public boolean isPlotRoad() {
        final PlotArea area = this.getPlotArea();
        return area != null && area.getPlotAbs(this) == null;
    }

    /**
     * Checks if anyone owns a plot at the current location.
     *
     * @return {@code true} if the location is a road, not a plot area, or if the plot is unclaimed.
     */
    public boolean isUnownedPlotArea() {
        final PlotArea area = this.getPlotArea();
        return area != null && area.getOwnedPlotAbs(this) == null;
    }

    /**
     * Get the absolute {@link Plot}, if any, that contains this location
     *
     * @return (Absolute) plot containing the location, or {code null}
     */
    @Nullable public Plot getPlotAbs() {
        final PlotArea area = this.getPlotArea();
        if (area != null) {
            return area.getPlotAbs(this);
        } else {
            return null;
        }
    }

    /**
     * Get the {@link Plot}, if any, that contains this location
     *
     * @return plot containing the location, or {code null}
     */
    @Nullable public Plot getPlot() {
        final PlotArea area = this.getPlotArea();
        if (area != null) {
            return area.getPlot(this);
        } else {
            return null;
        }
    }

    /**
     * Get the coordinates of the chunk that contains this location
     *
     * @return Chunk coordinates
     */
    @Nonnull public BlockVector2 getChunkLocation() {
        return BlockVector2.at(this.getX() >> 4, this.getZ() >> 4);
    }

    /**
     * Return a new location offset by the given coordinates
     *
     * @param x X offset
     * @param y Y offset
     * @param z Z offset
     * @return New location
     */
    @Nonnull public Location add(final int x, final int y, final int z) {
        return new Location(this.world, this.blockVector3.add(x, y, z), this.yaw, this.pitch);
    }

    /**
     * Return a new location using the given X coordinate
     *
     * @param x New X coordinate
     * @return New location
     */
    @Nonnull public Location withX(final int x) {
        return new Location(this.world, this.blockVector3.withX(x), this.yaw, this.pitch);
    }

    /**
     * Return a new location using the given Y coordinate
     *
     * @param y New Y coordinate
     * @return New location
     */
    @Nonnull public Location withY(final int y) {
        return new Location(this.world, this.blockVector3.withY(y), this.yaw, this.pitch);
    }

    /**
     * Return a new location using the given Z coordinate
     *
     * @param z New Z coordinate
     * @return New location
     */
    @Nonnull public Location withZ(final int z) {
        return new Location(this.world, this.blockVector3.withZ(z), this.yaw, this.pitch);
    }

    /**
     * Return a new location using the given yaw
     *
     * @param yaw New yaw
     * @return New location
     */
    @Nonnull public Location withYaw(final float yaw) {
        return new Location(this.world, this.blockVector3, yaw, this.pitch);
    }

    /**
     * Return a new location using the given pitch
     *
     * @param pitch New pitch
     * @return New location
     */
    @Nonnull public Location withPitch(final float pitch) {
        return new Location(this.world, this.blockVector3, this.yaw, pitch);
    }

    /**
     * Return a new location using the given world
     *
     * @param world New world
     * @return New location
     */
    @Nonnull public Location withWorld(@Nonnull final String world) {
        return new Location(world, this.blockVector3, this.yaw, this.pitch);
    }

    public double getEuclideanDistanceSquared(@Nonnull final Location l2) {
        double x = getX() - l2.getX();
        double y = getY() - l2.getY();
        double z = getZ() - l2.getZ();
        return x * x + y * y + z * z;
    }

    public double getEuclideanDistance(@Nonnull final Location l2) {
        return Math.sqrt(getEuclideanDistanceSquared(l2));
    }

    /**
     * Return a new location offset by (-) the given coordinates
     *
     * @param x X offset
     * @param y Y offset
     * @param z Z offset
     * @return New location
     */
    @Nonnull public Location subtract(int x, int y, int z) {
        return this.add(-x, -y, -z);
    }

    /**
     * Get a minimum bounding rectangle that contains this location only
     *
     * @return Minimum bounding rectangle
     */
    @Nonnull public MBR toMBR() {
        return new SimpleMBR(this.getX(), this.getX(), this.getY(), this.getY(), this.getZ(),
            this.getZ());
    }

    @Override public int compareTo(@Nonnull final Location o) {
        if (this.getX() == o.getX() && this.getY() == o.getY() || this.getZ() == o.getZ()) {
            return 0;
        }
        if (this.getX() < o.getX() && this.getY() < o.getY() && this.getZ() < o.getZ()) {
            return -1;
        }
        return 1;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Location location = (Location) o;
        return Float.compare(location.getYaw(), getYaw()) == 0
            && Float.compare(location.getPitch(), getPitch()) == 0 && Objects
            .equal(getBlockVector3(), location.getBlockVector3()) && Objects
            .equal(getWorld(), location.getWorld());
    }

    @Override public int hashCode() {
        return Objects
            .hashCode(super.hashCode(), getYaw(), getPitch(), getBlockVector3(), getWorld());
    }

    @Override public String toString() {
        return "\"plotsquaredlocation\":{\"x\":" + this.getX() + ",\"y\":" + this.getY() + ",\"z\":"
            + this.getZ() + ",\"yaw\":" + this.yaw + ",\"pitch\":" + this.pitch + ",\"world\":\""
            + this.world + "\"}";
    }

    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Location))
            return false;
        final Location other = (Location) o;
        if (Float.compare(this.yaw, other.yaw) != 0)
            return false;
        if (Float.compare(this.pitch, other.pitch) != 0)
            return false;
        final Object this$blockVector3 = this.blockVector3;
        final Object other$blockVector3 = other.blockVector3;
        if (this$blockVector3 == null ?
            other$blockVector3 != null :
            !this$blockVector3.equals(other$blockVector3))
            return false;
        final Object this$world = this.getWorld();
        final Object other$world = other.getWorld();
        if (this$world == null ? other$world != null : !this$world.equals(other$world))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + Float.floatToIntBits(this.yaw);
        result = result * PRIME + Float.floatToIntBits(this.pitch);
        final Object $blockVector3 = this.blockVector3;
        result = result * PRIME + ($blockVector3 == null ? 43 : $blockVector3.hashCode());
        final Object $world = this.getWorld();
        result = result * PRIME + ($world == null ? 43 : $world.hashCode());
        return result;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public BlockVector3 getBlockVector3() {
        return this.blockVector3;
    }
}
