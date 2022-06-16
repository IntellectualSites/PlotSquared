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
import com.plotsquared.core.plot.Rating;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PlotRateEvent extends PlotEvent implements CancellablePlotEvent {

    private final PlotPlayer<?> rater;
    @Nullable
    private Rating rating;
    private Result eventResult;

    /**
     * PlotRateEvent: Called when a player rates a plot
     *
     * @param rater  The player rating the plot
     * @param rating The rating being given
     * @param plot   The plot being rated
     */
    public PlotRateEvent(PlotPlayer<?> rater, @Nullable Rating rating, Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public PlotPlayer<?> getRater() {
        return this.rater;
    }

    public @Nullable Rating getRating() {
        return this.rating;
    }

    public void setRating(@Nullable Rating rating) {
        this.rating = rating;
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }

}
