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

import com.plotsquared.core.persistence.entity.PlayerMetaEntity;

import java.util.List;

/**
 * Repository for storing and retrieving arbitrary metadata entries for players.
 * Keys are namespaced per player UUID and map to binary values.
 */
public interface PlayerMetaRepository {
    /**
     * Returns all metadata entries for the player with the given UUID.
     *
     * @param uuid the player's UUID (String representation)
     * @return list of metadata entries, never null; may be empty
     */
    List<PlayerMetaEntity> findByUuid(String uuid);

    /**
     * Inserts or updates a metadata value for the given player and key.
     *
     * @param uuid  the player's UUID (String representation)
     * @param key   the metadata key
     * @param value the value as a byte array; implementations may store as-is
     */
    void put(String uuid, String key, byte[] value);

    /**
     * Deletes a metadata entry for the given player and key. No-op if absent.
     *
     * @param uuid the player's UUID (String representation)
     * @param key  the metadata key
     */
    void delete(String uuid, String key);
}
