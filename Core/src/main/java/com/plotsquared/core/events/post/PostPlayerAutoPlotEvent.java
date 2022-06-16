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
package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlotPlayerEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

/**
 * Called after a plot was chosen for a player by {@code /plot auto}
 * <br>
 * Called after {@link com.plotsquared.core.events.PlayerAutoPlotEvent} and only, if no listener cancelled the action.
 *
 * @since 6.2.0
 */
public class PostPlayerAutoPlotEvent extends PlotPlayerEvent {

    /**
     * Instantiate a new PlayerAutoPlotPostEvent.
     *
     * @param plotPlayer The player who claims a new plot by {@code /plot auto}.
     * @param plot       The plot that is assigned to the player.
     */
    public PostPlayerAutoPlotEvent(final PlotPlayer<?> plotPlayer, final Plot plot) {
        super(plotPlayer, plot);
    }

}
