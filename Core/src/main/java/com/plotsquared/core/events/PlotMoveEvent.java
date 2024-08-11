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
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Called when a {@link PlotPlayer} attempts to move a {@link Plot} to another {@link Plot}.
 *
 * <br>
 * <ul>
 * <li>{@link #getPlotPlayer()} is the initiator of the move action (most likely the command executor)</li>
 * <li>{@link #getPlot()} is the plot to be moved</li>
 * <li>{@link #destination()} is the plot, where the plot will be moved to.</li>
 * </ul>
 *
 * @since TODO
 */
public class PlotMoveEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private Plot destination;
    private Result result = Result.ACCEPT;

    public PlotMoveEvent(final PlotPlayer<?> initiator, final Plot plot, final Plot destination) {
        super(initiator, plot);
        this.destination = destination;
    }

    /**
     * @return The destination for the plot to be moved to.
     * @since TODO
     */
    public Plot destination() {
        return destination;
    }

    /**
     * Set the new {@link Plot} to where the plot should be moved to.
     *
     * @param destination The plot representing the new location.
     * @since TODO
     */
    public void setDestination(@NonNull final Plot destination) {
        this.destination = Objects.requireNonNull(destination);
    }

    /**
     * Set the new destination based off their X and Y coordinates. Calls {@link #setDestination(Plot)} while using the
     * {@link com.plotsquared.core.plot.PlotArea} provided by the current {@link #destination()}.
     * <p>
     * <b>Note:</b> the coordinates are not minecraft world coordinates, but the underlying {@link PlotId}s coordinates.
     *
     * @param x The X coordinate of the {@link PlotId}
     * @param y The Y coordinate of the {@link PlotId}
     */
    public void setDestination(final int x, final int y) {
        this.destination = Objects.requireNonNull(this.destination.getArea()).getPlot(PlotId.of(x, y));
    }

    @Override
    public Result getEventResult() {
        return result;
    }

    @Override
    public void setEventResult(Result eventResult) {
        this.result = Objects.requireNonNull(eventResult);
    }

}
