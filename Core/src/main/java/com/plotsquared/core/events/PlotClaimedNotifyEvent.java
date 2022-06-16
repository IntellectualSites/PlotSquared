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

import com.plotsquared.core.plot.Plot;

/**
 * To be used as a notification that a plot has been claimed. For cancelling events, see {@link PlayerClaimPlotEvent}
 */
public class PlotClaimedNotifyEvent extends PlotEvent {

    private final boolean auto;

    /**
     * New event instance.
     *
     * @param plot Plot that was claimed
     * @param auto If the plot was claimed using /plot auto
     * @since 6.1.0
     */
    public PlotClaimedNotifyEvent(Plot plot, boolean auto) {
        super(plot);
        this.auto = auto;
    }

    /**
     * If the plot was claimed using /plot auto
     *
     * @return if claimed with auto
     * @since 6.1.0
     */
    @SuppressWarnings("unused")
    public boolean wasAuto() {
        return auto;
    }

}
