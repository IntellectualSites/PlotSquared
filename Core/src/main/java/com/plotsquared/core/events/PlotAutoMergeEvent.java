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
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Event called when plots are automatically merged with /plot auto
 * {@inheritDoc}
 */
public final class PlotAutoMergeEvent extends PlotEvent implements CancellablePlotEvent {

    private final List<PlotId> plots;
    private final String world;
    private Result eventResult;

    /**
     * PlotAutoMergeEvent: Called when plots are automatically merged with /plot auto
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotAutoMergeEvent(
            final @NonNull String world, final @NonNull Plot plot,
            final @NonNull List<PlotId> plots
    ) {
        super(plot);
        this.world = world;
        this.plots = plots;
    }

    /**
     * Get the plots being added.
     *
     * @return Unmodifiable list containing the merging plots
     */
    public List<PlotId> getPlots() {
        return Collections.unmodifiableList(this.plots);
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }

    public String getWorld() {
        return this.world;
    }

}
