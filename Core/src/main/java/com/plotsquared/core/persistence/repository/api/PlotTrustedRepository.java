package com.plotsquared.core.persistence.repository.api;

import java.util.List;

/**
 * Repository for managing trusted users for a plot.
 * Trusted users typically have broader permissions than helpers but may be
 * distinct from owners and members depending on server policy.
 */
public interface PlotTrustedRepository {
    /**
     * Retrieves all trusted user UUIDs for the given plot.
     *
     * @param plotId the unique identifier of the plot
     * @return list of trusted user UUIDs (as String), never null; may be empty
     */
    List<String> findUsers(long plotId);

    /**
     * Adds the given user to the trusted list of the specified plot.
     * Implementations should be idempotent and avoid duplicates.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void add(long plotId, String userUuid);

    /**
     * Removes the given user from the trusted list of the specified plot.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void remove(long plotId, String userUuid);
}
