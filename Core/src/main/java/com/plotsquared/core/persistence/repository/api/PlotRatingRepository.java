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

import com.plotsquared.core.persistence.entity.PlotRatingEntity;

import java.util.List;

/**
 * Repository for managing player ratings of plots.
 */
public interface PlotRatingRepository {
    /**
     * Retrieves all rating entries for the given plot.
     *
     * @param plotId the plot identifier
     * @return list of plot ratings, never null; may be empty
     */
    List<PlotRatingEntity> findByPlotId(long plotId);

    /**
     * Inserts a new rating or updates the existing rating for the given player on the plot.
     * The rating scale is defined by the caller/implementation.
     *
     * @param plotId     the plot identifier
     * @param playerUuid the player's UUID (String representation)
     * @param rating     the rating value to set
     */
    void upsert(long plotId, String playerUuid, int rating);
}
