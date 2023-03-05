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
package com.plotsquared.core.services.plots;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Crate a new auto query
 *
 * @param player   Player to claim for
 * @param startId  Plot ID to start searching from
 * @param sizeX    Number of plots along the X axis
 * @param sizeZ    Number of plots along the Z axis
 * @param plotArea Plot area to search in
 */
public record AutoQuery(
        @NonNull PlotPlayer<?> player,
        @Nullable PlotId startId,
        int sizeX,
        int sizeZ,
        @NonNull PlotArea plotArea
) {

    /**
     * Get the player that the plots are meant for
     *
     * @return Player
     */
    @Override
    public @NonNull PlotPlayer<?> player() {
        return this.player;
    }

    /**
     * Get the plot ID to start searching from
     *
     * @return Start ID
     */
    @Override
    public @Nullable PlotId startId() {
        return this.startId;
    }

    /**
     * Get the number of plots along the X axis
     *
     * @return Number of plots along the X axis
     */
    @Override
    public int sizeX() {
        return this.sizeX;
    }

    /**
     * Get the number of plots along the Z axis
     *
     * @return Number of plots along the Z axis
     */
    @Override
    public int sizeZ() {
        return this.sizeZ;
    }

    /**
     * Get the plot area to search in
     *
     * @return Plot area
     */
    @Override
    public @NonNull PlotArea plotArea() {
        return this.plotArea;
    }

}
