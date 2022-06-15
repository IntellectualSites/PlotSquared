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
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class PlotEvent {

    private final Plot plot;
    private String name;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    /**
     * Obtain the plot involved in the event
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    /**
     * Obtain the event's class name
     *
     * @return the event class name
     */
    public @NonNull String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }

}
