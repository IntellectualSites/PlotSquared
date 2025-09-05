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
 * Repository abstraction for managing invited users for a cluster.
 * Implementations persist associations between clusters and player UUIDs
 * that have been invited to the cluster but may not yet be members/helpers.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
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
