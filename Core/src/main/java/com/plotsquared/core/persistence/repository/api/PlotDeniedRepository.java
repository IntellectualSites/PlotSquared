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
 * Repository for managing denied users for a plot.
 * Denied users are explicitly prevented from interacting with or entering the plot
 * depending on gameplay rules. This repository stores associations between plot ids
 * and player UUIDs.
 *
 * @since 8.0.0
 * @version 1.0.0
 * @author TheMeinerLP
 * @author IntellectualSites
 */
public interface PlotDeniedRepository {
    /**
     * Retrieves all denied user UUIDs for the given plot.
     *
     * @param plotId the unique identifier of the plot
     * @return list of denied user UUIDs (as String), never null; may be empty
     */
    List<String> findUsers(long plotId);

    /**
     * Adds the given user to the denied list of the specified plot.
     * Implementations should be idempotent and avoid duplicates.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void add(long plotId, String userUuid);

    /**
     * Removes the given user from the denied list of the specified plot.
     *
     * @param plotId   the unique identifier of the plot
     * @param userUuid the user's UUID (String representation)
     */
    void remove(long plotId, String userUuid);

    /**
     * Removes all denied users for the specified plot.
     *
     * @param plotId the unique identifier of the plot
     */
    void deleteByPlotId(long plotId);
}
