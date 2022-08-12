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

import java.util.Objects;

/**
 * Called when a {@link PlotPlayer} attempts to move a {@link Plot} to another {@link Plot}.
 * The Event-Result {@link Result#FORCE} does have no effect on the outcome. Only supported results are {@link Result#DENY} and
 * {@link Result#ACCEPT}.
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

    private final Plot destination;
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


    @Override
    public Result getEventResult() {
        return result;
    }

    @Override
    public void setEventResult(Result eventResult) {
        this.result = Objects.requireNonNull(eventResult);
    }

}
