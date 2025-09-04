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

    void movePlots(Plot originPlot, Plot newPlot);

    void setOwner(Plot plot, UUID newOwner);

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
}
