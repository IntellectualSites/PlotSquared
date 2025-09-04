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
