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
package com.plotsquared.core.events;

import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Called every time after PlotSquared calculated a players plot limit based on their permission.
 * <p>
 * May be used to grant a player more plots based on another rank or bought feature.
 *
 * @since 7.3.0
 */
public class PlayerPlotLimitEvent {

    private final PlotPlayer<?> player;

    private int limit;

    public PlayerPlotLimitEvent(@NonNull final PlotPlayer<?> player, @NonNegative final int limit) {
        this.player = player;
        this.limit = limit;
    }

    /**
     * Overrides the previously calculated or set plot limit for {@link #player()}.
     *
     * @param limit The amount of plots a player may claim. Must be {@code 0} or greater.
     * @since 7.3.0
     */
    public void limit(@NonNegative final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Player plot limit must be greater or equal 0");
        }
        this.limit = limit;
    }

    /**
     * Returns the previous set limit, if none was overridden before this event handler the default limit based on the players
     * permissions node is returned.
     *
     * @return The currently defined plot limit of this player.
     * @since 7.3.0
     */
    public @NonNegative int limit() {
        return limit;
    }

    /**
     * The player for which the limit is queried.
     *
     * @return the player.
     * @since 7.3.0
     */
    public @NonNull PlotPlayer<?> player() {
        return player;
    }

}
