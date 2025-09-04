package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotRatingEntity;

import java.util.List;

/**
 * Repository for managing player ratings of plots.
 */
public interface PlotRatingRepository {
    /**
     * Retrieves all rating entries for the given plot.
     *
     * @param plotId the plot identifier
     * @return list of plot ratings, never null; may be empty
     */
    List<PlotRatingEntity> findByPlotId(long plotId);

    /**
     * Inserts a new rating or updates the existing rating for the given player on the plot.
     * The rating scale is defined by the caller/implementation.
     *
     * @param plotId     the plot identifier
     * @param playerUuid the player's UUID (String representation)
     * @param rating     the rating value to set
     */
    void upsert(long plotId, String playerUuid, int rating);
}
