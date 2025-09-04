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

import java.util.List;

/**
 * Repository abstraction for managing helper users associated with a cluster.
 * Implementations are responsible for persisting and retrieving associations
 * between a cluster and player UUIDs who act as helpers.
 */
public interface ClusterHelperRepository {
    /**
     * Retrieves all helper user UUIDs associated with the given cluster.
     *
     * @param clusterId the unique identifier of the cluster
     * @return list of helper user UUIDs (as String), never null; may be empty
     */
    List<String> findUsers(long clusterId);

    /**
     * Adds the given user as a helper to the specified cluster.
     * Implementations should be idempotent: adding an existing association
     * should not create duplicates nor fail.
     *
     * @param clusterId the unique identifier of the cluster
     * @param userUuid  the helper user's UUID (String representation)
     */
    void add(long clusterId, String userUuid);

    /**
     * Removes the given user from the helpers of the specified cluster.
     *
     * @param clusterId the unique identifier of the cluster
     * @param userUuid  the helper user's UUID (String representation)
     */
    void remove(long clusterId, String userUuid);
}
