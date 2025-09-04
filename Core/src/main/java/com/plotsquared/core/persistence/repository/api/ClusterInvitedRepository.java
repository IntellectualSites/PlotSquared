package com.plotsquared.core.persistence.repository.api;

import java.util.List;

/**
 * Repository abstraction for managing invited users for a cluster.
 * Implementations persist associations between clusters and player UUIDs
 * that have been invited to the cluster but may not yet be members/helpers.
 */
public interface ClusterInvitedRepository {
    /**
     * Retrieves all invited user UUIDs for the given cluster.
     *
     * @param clusterId the unique identifier of the cluster
     * @return list of invited user UUIDs (as String), never null; may be empty
     */
    List<String> findUsers(long clusterId);

    /**
     * Records an invitation of the given user to the specified cluster.
     * Implementations should treat duplicate invitations idempotently.
     *
     * @param clusterId the unique identifier of the cluster
     * @param userUuid  the invited user's UUID (String representation)
     */
    void add(long clusterId, String userUuid);

    /**
     * Revokes an invitation for the given user from the specified cluster.
     *
     * @param clusterId the unique identifier of the cluster
     * @param userUuid  the invited user's UUID (String representation)
     */
    void remove(long clusterId, String userUuid);
}
