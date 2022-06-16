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

import com.plotsquared.core.location.Location;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * A world that contains plots
 */
public abstract class PlotWorld {

    private final String world;

    /**
     * Create a new plot world with a given world name
     *
     * @param world World name
     */
    protected PlotWorld(final @NonNull String world) {
        this.world = world;
    }

    /**
     * Get the plot area that contains the given location, or null
     * if the location is not a part of a plot area.
     *
     * @param location Location
     * @return Containing plot area, or null
     */
    public @Nullable
    abstract PlotArea getArea(final @NonNull Location location);

    /**
     * Get all plot areas in the world
     *
     * @return All plot areas in the world
     */
    public @NonNull
    abstract Collection<PlotArea> getAreas();

    /**
     * Get all plot areas in a specified region
     *
     * @param region Region
     * @return All areas in the region
     */
    public @NonNull
    abstract Collection<PlotArea> getAreasInRegion(
            final @NonNull CuboidRegion region
    );

    /**
     * Register a new area in the world
     *
     * @param area Plot area
     */
    public void addArea(final @NonNull PlotArea area) {
        throw new UnsupportedOperationException("This world type does not allow adding new areas");
    }

    /**
     * Remove an area from the world
     *
     * @param area Plot area
     */
    public void removeArea(final @NonNull PlotArea area) {
        throw new UnsupportedOperationException("This world type does not allow removing areas");
    }

    /**
     * Get the world name
     *
     * @return World name
     */
    public String getWorld() {
        return this.world;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PlotWorld plotWorld = (PlotWorld) o;
        return world.equals(plotWorld.world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

}
