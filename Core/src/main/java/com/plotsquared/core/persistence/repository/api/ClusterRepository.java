package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.ClusterEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing ClusterEntity persistence and lookups.
 */
public interface ClusterRepository {
    /**
     * Finds a cluster by its primary identifier.
     *
     * @param id the cluster id
     * @return an Optional containing the ClusterEntity if found, otherwise empty
     */
    Optional<ClusterEntity> findById(long id);

    /**
     * Finds the cluster by world and a coordinate that lies within the cluster bounds.
     * Exact semantics depend on implementation; typically returns the cluster that contains
     * the given x/z coordinate in the specified world.
     *
     * @param world the world name
     * @param x     x-coordinate (plot coordinate or block coordinate as defined by implementation)
     * @param z     z-coordinate (plot coordinate or block coordinate as defined by implementation)
     * @return an Optional containing the ClusterEntity if a match is found; otherwise empty
     */
    Optional<ClusterEntity> findByWorldAndBounds(String world, int x, int z);

    /**
     * Returns all clusters in a given world.
     *
     * @param world the world name
     * @return list of clusters, never null; may be empty
     */
    List<ClusterEntity> findByWorld(String world);

    /**
     * Persists the given cluster. Implementations may insert or update as needed.
     *
     * @param cluster the cluster entity to save
     */
    void save(ClusterEntity cluster);

    /**
     * Deletes the cluster with the specified id. No-op if it does not exist.
     *
     * @param id the cluster id
     */
    void deleteById(long id);
}
