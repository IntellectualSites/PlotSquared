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

import com.plotsquared.core.persistence.entity.ClusterEntity;
import com.plotsquared.core.plot.PlotId;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing ClusterEntity persistence and lookups.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
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
     * Returns all clusters across all worlds.
     */
    List<ClusterEntity> findAll();

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

    /**
     * Update world for all clusters from oldWorld to newWorld.
     */
    void updateWorldAll(String oldWorld, String newWorld);

    /**
     * Update world for clusters overlapping the given bounds in oldWorld.
     */
    void updateWorldInBounds(String oldWorld, String newWorld, int minX, int minZ, int maxX, int maxZ);

    /**
     * Update world for all clusters from oldWorld to newWorld.
     */
    void replaceWorld(String oldWorld, String newWorld);

    /**
     * Update world for clusters overlapping the given bounds in oldWorld.
     */
    void replaceWorldInBounds(String oldWorld, String newWorld, PlotId min, PlotId max);
}
