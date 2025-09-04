package com.plotsquared.core.persistence.repository.api;

import java.util.List;

/**
 * Repository for managing denied users for a plot.
 * Denied users are explicitly prevented from interacting with or entering the plot
 * depending on gameplay rules. This repository stores associations between plot ids
 * and player UUIDs.
 */
public interface PlotDeniedRepository {
    /**
     * Retrieves all denied user UUIDs for the given plot.
     *
     * @param plotId the unique identifier of the plot
     * @return list of denied user UUIDs (as String), never null; may be empty
     */
    List<String> findUsers(long plotId);

    /**
     * Adds the given user to the denied list of the specified plot.
     * Implementations should be idempotent and avoid duplicates.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void add(long plotId, String userUuid);

    /**
     * Removes the given user from the denied list of the specified plot.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void remove(long plotId, String userUuid);

    /**
     * Removes all denied users for the specified plot.
     *
     * @param plotId the unique identifier of the plot
     */
    void deleteByPlotId(long plotId);
}
