package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.ClusterSettingsEntity;

import java.util.Optional;

/**
 * Repository abstraction for managing cluster settings operations.
 */
public interface ClusterSettingsRepository {
    /**
     * Find settings for a given cluster id.
     *
     * @param clusterId the cluster id
     * @return optional settings entity
     */
    Optional<ClusterSettingsEntity> findById(long clusterId);

    /**
     * Update the "position" setting for a cluster.
     *
     * @param clusterId the cluster id
     * @param position  the new position string
     */
    void updatePosition(long clusterId, String position);
}
