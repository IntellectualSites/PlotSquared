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
package com.plotsquared.core.plot.world;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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
    @Nullable PlotArea getPlotArea(@NonNull Location location);

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
    @Nullable PlotArea getPlotArea(@NonNull String world, @Nullable String id);

    /**
     * Get all plot areas in a world, with an optional region constraint
     *
     * @param world  World name
     * @param region Optional region
     * @return All plots in the region
     */
    @NonNull PlotArea[] getPlotAreas(@NonNull String world, @Nullable CuboidRegion region);

    /**
     * Get all plot areas recognized by PlotSquared
     *
     * @return All plot areas
     */
    @NonNull PlotArea[] getAllPlotAreas();

    /**
     * Get all worlds recognized by PlotSquared
     *
     * @return All world names
     */
    @NonNull String[] getAllWorlds();

    /**
     * Add a plot area
     *
     * @param area Area
     */
    void addPlotArea(@NonNull PlotArea area);

    /**
     * Remove a plot area
     *
     * @param area Area
     */
    void removePlotArea(@NonNull PlotArea area);

    /**
     * Add a world
     *
     * @param worldName Name of the world to add
     * @return {@code true} if successful, {@code false} if world already existed
     * @since 7.0.0
     */
    boolean addWorld(@NonNull String worldName);

    /**
     * Remove a world
     *
     * @param worldName Name of the world to remove
     */
    void removeWorld(@NonNull String worldName);

    /**
     * Method that delegates to {@link #getPlotAreas(String, CuboidRegion)} but returns an
     * immutable set, instead of an array
     *
     * @param world  World name
     * @param region Optional region
     * @return All areas in the world (and region)
     */
    default @NonNull Set<@NonNull PlotArea> getPlotAreasSet(
            final @NonNull String world,
            final @Nullable CuboidRegion region
    ) {
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
    default @NonNull Set<@NonNull PlotArea> getPlotAreasSet(final @NonNull String world) {
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
    default @Nullable PlotArea getPlotAreaByString(final @NonNull String search) {
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
     * <p>
     * Use {@link #getPlotAreaByString(String)} to get the PlotArea object
     * </p>
     *
     * @param world the world
     * @return if a plot world is registered
     */
    default boolean hasPlotArea(final @NonNull String world) {
        return this.getPlotAreas(world, null).length != 0;
    }

    /**
     * Check if a given world is an augmented plot world
     *
     * @param world World name
     * @return {@code true} if the world is augmented plot world, {@code false} if not
     */
    default boolean isAugmented(final @NonNull String world) {
        final PlotArea[] areas = this.getPlotAreas(world, null);
        return areas != null && (areas.length > 1 || areas[0].getType() != PlotAreaType.NORMAL);
    }

    /**
     * Perform an action on each recognized plot area
     *
     * @param action Action to perform
     */
    default void forEachPlotArea(final @NonNull Consumer<? super PlotArea> action) {
        for (final PlotArea area : this.getAllPlotAreas()) {
            action.accept(area);
        }
    }

}
