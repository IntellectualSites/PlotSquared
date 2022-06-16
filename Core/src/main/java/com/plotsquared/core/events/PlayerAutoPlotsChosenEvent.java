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
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Event fired when the plots that are to be claimed by a player executing a /plot auto have been chosen. It contains an
 * unmodifiable list of the plots selected. This may be of length 0. This event is effectively cancellable by setting the list
 * of plots to an empty list.
 */
public class PlayerAutoPlotsChosenEvent extends PlotPlayerEvent {

    private List<Plot> plots;

    /**
     * PlayerAutoPlotsChosenEvent: Called when one or more plots are chosen for a /plot auto
     *
     * @param player Player that executed the auto
     * @param plots  Plots that have been chosen to be set to the player
     * @since 6.1.0
     */
    public PlayerAutoPlotsChosenEvent(PlotPlayer<?> player, List<Plot> plots) {
        super(player, plots.size() > 0 ? plots.get(0) : null);
        this.plots = Collections.unmodifiableList(plots);
    }

    /**
     * Returns the plot at index 0 in the list of plots selected. May be null if the list was of length 0.
     *
     * @return plot at index 0 or null.
     */
    @Override
    public @Nullable Plot getPlot() {
        return super.getPlot();
    }

    /**
     * Get the immutable list of plots selected to be claimed by the player. May be of length 0.
     *
     * @return immutable list.
     * @since 6.1.0
     */
    public @NonNull List<Plot> getPlots() {
        return plots;
    }

    /**
     * Set the plots to be claimed by the player.
     *
     * @param plots list of plots.
     * @since 6.1.0
     */
    public void setPlots(final @NonNull List<Plot> plots) {
        this.plots = List.copyOf(plots);
    }

}
