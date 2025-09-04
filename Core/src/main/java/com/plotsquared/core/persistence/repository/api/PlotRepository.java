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
package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotEntity;
import com.plotsquared.core.plot.Plot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository abstraction for reading and writing plot data and related
 * lookups by id, world, owner, and coordinates.
 */
public interface PlotRepository {
    /**
     * Swaps the persisted state of two plots.
     * Implementations should ensure the swap occurs atomically/consistently
     * if the underlying storage supports transactions.
     *
     * @param plot1 the first plot
     * @param plot2 the second plot
     * @return true if the swap succeeded; false otherwise
     */
    boolean swapPlots(Plot plot1, Plot plot2);

    void replaceWorldAll(String oldWorld, String newWorld);

    void movePlots(Plot originPlot, Plot newPlot);

    void setOwner(Plot plot, UUID newOwner);

    /**
     * Bulk update world name for plots within the given rectangular plot-id range.
     *
     * @param oldWorld the source world name
     * @param newWorld the destination world name
     * @param minX     minimum plot x-id (inclusive)
     * @param minZ     minimum plot z-id (inclusive)
     * @param maxX     maximum plot x-id (inclusive)
     * @param maxZ     maximum plot z-id (inclusive)
     */
    void replaceWorldInRange(String oldWorld, String newWorld, int minX, int minZ, int maxX, int maxZ);

    /**
     * Bulk update world name for all plots in a given world.
     */
    void replaceWorld(String oldWorld, String newWorld);

    /**
     * Bulk update world name for plots within the given rectangular plot-id bounds.
     */
    void replaceWorldInBounds(String oldWorld, String newWorld, com.plotsquared.core.plot.PlotId min, com.plotsquared.core.plot.PlotId max);

    /**
     * Bulk-creates plots and associated data (settings, flags, memberships)
     * in a single transactional operation.
     *
     * @param plots list of plots to persist
     */
    void createPlotsAndData(List<Plot> plots);

    /**
     * Creates a plot if it does not already exist for the given world/x/z.
     * Also creates an empty settings row. Mirrors legacy createPlotSafe behavior.
     * Sets plot.temp to the generated id if created.
     *
     * @param plot plot to create
     * @return true if a new plot was created; false if it already existed
     */
    boolean createPlotSafe(Plot plot);

    /**
     * Creates a plot and an empty settings row. Mirrors legacy createPlotAndSettings.
     * Sets plot.temp to the generated id.
     *
     * @param plot plot to create
     */
    void createPlotAndSettings(Plot plot);

    /**
     * Load all plots (with settings, flags and tiers) grouped by world and PlotId.
     * Mirrors the legacy SQLManager#getPlots structure.
     *
     * @return world -> (PlotId -> Plot)
     */
    java.util.HashMap<String, java.util.HashMap<com.plotsquared.core.plot.PlotId, Plot>> getPlots();

    /**
     * Finds a plot by its primary identifier.
     *
     * @param id the plot id
     * @return an Optional with the plot entity if found; otherwise empty
     */
    Optional<PlotEntity> findById(long id);

    /**
     * Finds a plot by its x and z coordinates.
     * The exact semantics are implementation-defined; despite the name, some
     * implementations may also use the provided world hint.
     *
     * @param x     plot x-id (not block coordinate)
     * @param z     plot z-id (not block coordinate)
     * @param world an optional world hint depending on implementation
     * @return an Optional with the plot entity if found; otherwise empty
     */
    Optional<PlotEntity> findByXAndZAnyWorld(int x, int z, String world);

    /**
     * Finds a plot by world and plot coordinates.
     *
     * @param world the world name
     * @param x     plot x-id (not block coordinate)
     * @param z     plot z-id (not block coordinate)
     * @return an Optional with the plot entity if found; otherwise empty
     */
    Optional<PlotEntity> findByWorldAndId(String world, int x, int z);

    /**
     * Finds all plots owned by the given player.
     *
     * @param ownerUuid the owner's UUID (String representation)
     * @return list of plots, never null; may be empty
     */
    List<PlotEntity> findByOwner(String ownerUuid);

    /**
     * Finds all plots in the specified world.
     *
     * @param world the world name
     * @return list of plots, never null; may be empty
     */
    List<PlotEntity> findByWorld(String world);

    /**
     * Persists the given plot entity (insert or update).
     *
     * @param plot the plot entity to save
     */
    void save(PlotEntity plot);

    /**
     * Deletes the plot with the specified id. No-op if it does not exist.
     *
     * @param id the plot id
     */
    void deleteById(long id);

    /**
     * Deletes all ratings for the given plot.
     *
     * @param plot the plot whose ratings should be removed
     */
    void deleteRatings(Plot plot);

    /**
     * Deletes the plot and all related data (flags, helpers, trusted, denied, ratings, and settings via cascade).
     * No-op if the plot does not exist.
     *
     * @param plot the plot to delete
     */
    void delete(Plot plot);

    /**
     * Purge plots and all related data by internal database ids.
     */
    void purgeIds(java.util.Set<Integer> ids);

    /**
     * Purge plots by world and plot-id coordinates.
     */
    void purgeByWorldAndPlotIds(String world, java.util.Set<com.plotsquared.core.plot.PlotId> plotIds);
}
