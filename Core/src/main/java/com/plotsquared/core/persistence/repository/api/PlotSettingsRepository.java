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

import com.plotsquared.core.persistence.entity.PlotSettingsEntity;

import java.util.Optional;

/**
 * Repository for persisting and retrieving per-plot settings.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
public interface PlotSettingsRepository {
    /**
     * Retrieves the settings for the given plot if available.
     *
     * @param plotId the plot identifier
     * @return Optional with the settings if present; otherwise empty
     */
    Optional<PlotSettingsEntity> findByPlotId(long plotId);

    /**
     * Persists the provided settings entity (insert or update).
     *
     * @param settings the settings entity to save
     */
    void save(PlotSettingsEntity settings);

    /**
     * Deletes the settings associated with the specified plot id. No-op if absent.
     *
     * @param plotId the plot identifier
     */
    void deleteByPlotId(long plotId);

    /**
     * Updates the alias for the plot settings row of the given plot id.
     */
    void updateAlias(long plotId, String alias);

    /**
     * Updates the home position string for the plot settings row of the given plot id.
     */
    void updatePosition(long plotId, String position);

    /**
     * Updates the merged bitmask for the plot settings row of the given plot id.
     */
    void updateMerged(long plotId, int mergedBitmask);

    /**
     * Creates a default settings row for the plot if it does not exist.
     */
    void createDefaultIfAbsent(long plotId, String defaultPosition);
}
