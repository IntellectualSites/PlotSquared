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
package com.plotsquared.core.plot.world;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface PlotAreaManager {

    /**
     * Get the plot area for a particular location. This
     * method assumes that the caller already knows that
     * the location belongs to a plot area, in which
     * case it will return the appropriate plot area.
     * <p>
     * If the location does not belong to a plot area,
     * it may still return an area.
     *
     * @param location The location
     * @return An applicable area, or null
     */
    @Nullable PlotArea getApplicablePlotArea(@Nullable Location location);

    /**
     * Get the plot area, if there is any, for the given
     * location. This may return null, if given location
     * does not belong to a plot area.
     *
     * @param location The location
     * @return The area if found, else {@code null}
     */
    @Nullable PlotArea getPlotArea(@NotNull Location location);

    /**
     * Get the plot area in a world with an (optional ID).
     * If the world has more than one plot area, and ID must be
     * supplied. If the world only has one plot area, the ID will
     * be ignored
     *
     * @param world World name
     * @param id    Area ID
     * @return Plot area matching the criteria
     */
    @Nullable PlotArea getPlotArea(@NotNull String world, @Nullable String id);

    /**
     * Get all plot areas in a world, with an optional region constraint
     *
     * @param world  World name
     * @param region Optional region
     * @return All plots in the region
     */
    @NotNull PlotArea[] getPlotAreas(@NotNull String world, @Nullable CuboidRegion region);

    /**
     * Get all plot areas recognized by PlotSquared
     *
     * @return All plot areas
     */
    @NotNull PlotArea[] getAllPlotAreas();

    /**
     * Get all worlds recognized by PlotSquared
     *
     * @return All world names
     */
    @NotNull String[] getAllWorlds();

    /**
     * Add a plot area
     *
     * @param area Area
     */
    void addPlotArea(@NotNull PlotArea area);

    /**
     * Remove a plot area
     *
     * @param area Area
     */
    void removePlotArea(@NotNull PlotArea area);

    /**
     * Add a world
     *
     * @param worldName Name of the world to add
     */
    void addWorld(@NotNull String worldName);

    /**
     * Remove a world
     *
     * @param worldName Name of the world to remove
     */
    void removeWorld(@NotNull String worldName);

    /**
     * Method that delegates to {@link #getPlotAreas(String, CuboidRegion)} but returns an
     * immutable set, instead of an array
     *
     * @param world  World name
     * @param region Optional region
     * @return All areas in the world (and region)
     */
    @NotNull default Set<PlotArea> getPlotAreasSet(@NotNull final String world,
        @Nullable final CuboidRegion region) {
        final PlotArea[] areas = this.getPlotAreas(world, region);
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, areas);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Method identical to {@link #getPlotAreasSet(String, CuboidRegion)} but that
     * does not take in a region, and returns a modifiable set
     *
     * @param world World name
     * @return Modifiable set containing all plot areas in the specified world
     */
    @NotNull default Set<PlotArea> getPlotAreasSet(@NotNull final String world) {
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, this.getPlotAreas(world, null));
        return set;
    }

    /**
     * Get a plot area from a search string in the format "world;id" or "world,id"
     * where the ID portion is optional
     *
     * @param search Search string
     * @return An area that matches the search string, or {@code null}
     */
    @Nullable default PlotArea getPlotAreaByString(@NotNull final String search) {
        String[] split = search.split("[;,]");
        PlotArea[] areas = this.getPlotAreas(split[0], null);
        if (areas == null) {
            for (PlotArea area : this.getAllPlotAreas()) {
                if (area.getWorldName().equalsIgnoreCase(split[0])) {
                    if (area.getId() == null || split.length == 2 && area.getId()
                        .equalsIgnoreCase(split[1])) {
                        return area;
                    }
                }
            }
            return null;
        }
        if (areas.length == 1) {
            return areas[0];
        } else if (split.length == 1) {
            return null;
        } else {
            for (PlotArea area : areas) {
                if (StringMan.isEqual(split[1], area.getId())) {
                    return area;
                }
            }
            return null;
        }
    }

    /**
     * Check if a plot world.
     *
     * @param world the world
     * @return if a plot world is registered
     * @see #getPlotAreaByString(String) to get the PlotArea object
     */
    default boolean hasPlotArea(@NotNull final String world) {
        return this.getPlotAreas(world, null).length != 0;
    }

}
