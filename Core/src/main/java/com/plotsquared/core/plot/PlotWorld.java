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
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A world that contains plots
 */
@EqualsAndHashCode
public abstract class PlotWorld {

    private final String world;

    /**
     * Create a new plot world with a given world name
     *
     * @param world World name
     */
    protected PlotWorld(@NotNull final String world) {
        this.world = world;
    }

    /**
     * Get the plot area that contains the given location, or null
     * if the location is not a part of a plot area.
     *
     * @param location Location
     * @return Containing plot area, or null
     */
    @Nullable public abstract PlotArea getArea(@NotNull final Location location);

    /**
     * Get all plot areas in the world
     *
     * @return All plot areas in the world
     */
    @NotNull public abstract Collection<PlotArea> getAreas();

    /**
     * Get all plot areas in a specified region
     *
     * @param region Region
     * @return All areas in the region
     */
    @NotNull public abstract Collection<PlotArea> getAreasInRegion(
        @NotNull final CuboidRegion region);

    /**
     * Register a new area in the world
     *
     * @param area Plot area
     */
    public void addArea(@NotNull final PlotArea area) {
        throw new UnsupportedOperationException("This world type does not allow adding new areas");
    }

    /**
     * Remove an area from the world
     *
     * @param area Plot area
     */
    public void removeArea(@NotNull final PlotArea area) {
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

}
