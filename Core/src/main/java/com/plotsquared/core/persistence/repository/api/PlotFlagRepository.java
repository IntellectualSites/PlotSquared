package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotFlagEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing flags associated with a plot.
 * A flag is a named configuration entry applied to an individual plot.
 */
public interface PlotFlagRepository {
    /**
     * Retrieves all flags for a given plot.
     *
     * @param plotId the plot identifier
     * @return list of plot flags, never null; may be empty
     */
    List<PlotFlagEntity> findByPlotId(long plotId);

    /**
     * Retrieves a specific flag by name for the given plot.
     *
     * @param plotId   the plot identifier
     * @param flagName the flag name
     * @return Optional with the flag if present; otherwise empty
     */
    Optional<PlotFlagEntity> findByPlotAndName(long plotId, String flagName);

    /**
     * Persists the flag entity. Implementations may insert or update as needed.
     *
     * @param entity the flag entity to save
     */
    void save(PlotFlagEntity entity);

    /**
     * Deletes the flag with the given name from the specified plot.
     *
     * @param plotId   the plot identifier
     * @param flagName the flag name
     */
    void deleteByPlotAndName(long plotId, String flagName);
}
