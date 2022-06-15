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

import com.plotsquared.core.events.PlotEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.plot.Plot;

/**
 * Called when several plots were unlinked.
 *
 * @since 6.2.0
 */
public class PostPlotUnlinkEvent extends PlotEvent {

    private final PlotUnlinkEvent.REASON reason;

    /**
     * Instantiate a new PlotUnlinkedEvent.
     *
     * @param plot   The unlinked plot.
     * @param reason The reason for the unlink.
     */
    public PostPlotUnlinkEvent(final Plot plot, PlotUnlinkEvent.REASON reason) {
        super(plot);
        this.reason = reason;
    }

    /**
     * The original reason provided by {@link PlotUnlinkEvent}.
     *
     * @return The reason for the unlink.
     */
    public PlotUnlinkEvent.REASON getReason() {
        return reason;
    }

}
