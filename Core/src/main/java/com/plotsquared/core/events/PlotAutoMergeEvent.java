/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.events;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;

import javax.annotation.Nonnull;
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
    public PlotAutoMergeEvent(@Nonnull final String world, @Nonnull final Plot plot,
        @Nonnull final List<PlotId> plots) {
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

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }

    public String getWorld() {
        return this.world;
    }
}
