package com.plotsquared.core.persistence.repository.api;

import java.util.List;

/**
 * Repository for managing plot member associations.
 * Members typically have elevated permissions on the plot compared to visitors.
 */
public interface PlotMembershipRepository {
    /**
     * Retrieves all member user UUIDs for the given plot.
     *
     * @param plotId the unique identifier of the plot
     * @return list of member user UUIDs (as String), never null; may be empty
     */
    List<String> findUsers(long plotId);

    /**
     * Adds the given user as a member of the specified plot.
     * Implementations should be idempotent and avoid duplicates.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void add(long plotId, String userUuid);

    /**
     * Removes the given user from the members of the specified plot.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void remove(long plotId, String userUuid);
}
