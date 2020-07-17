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
package com.plotsquared.core.plot;

import com.plotsquared.core.location.Location;
import com.sk89q.worldedit.regions.CuboidRegion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

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
    protected PlotWorld(@Nonnull final String world) {
        this.world = world;
    }

    /**
     * Get the plot area that contains the given location, or null
     * if the location is not a part of a plot area.
     *
     * @param location Location
     * @return Containing plot area, or null
     */
    @Nullable public abstract PlotArea getArea(@Nonnull final Location location);

    /**
     * Get all plot areas in the world
     *
     * @return All plot areas in the world
     */
    @Nonnull public abstract Collection<PlotArea> getAreas();

    /**
     * Get all plot areas in a specified region
     *
     * @param region Region
     * @return All areas in the region
     */
    @Nonnull public abstract Collection<PlotArea> getAreasInRegion(
        @Nonnull final CuboidRegion region);

    /**
     * Register a new area in the world
     *
     * @param area Plot area
     */
    public void addArea(@Nonnull final PlotArea area) {
        throw new UnsupportedOperationException("This world type does not allow adding new areas");
    }

    /**
     * Remove an area from the world
     *
     * @param area Plot area
     */
    public void removeArea(@Nonnull final PlotArea area) {
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

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlotWorld)) {
            return false;
        }
        final PlotWorld other = (PlotWorld) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$world = this.getWorld();
        final Object other$world = other.getWorld();
        return Objects.equals(this$world, other$world);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PlotWorld;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $world = this.getWorld();
        result = result * PRIME + ($world == null ? 43 : $world.hashCode());
        return result;
    }
}
