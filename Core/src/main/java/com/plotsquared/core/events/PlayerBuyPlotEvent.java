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
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Called when a user attempts to buy a plot.
 * <p>
 * Setting the {@link #setEventResult(Result) Result} to {@link Result#FORCE} ignores the price and players account balance and does not charge the
 * player anything. {@link Result#DENY} blocks the purchase completely, {@link Result#ACCEPT} and {@code null} do not modify
 * the behaviour.
 * <p>
 * Setting the {@link #setPrice(double) price} to {@code 0} makes the plot practically free.
 *
 * @since 7.3.2
 */
public class PlayerBuyPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private Result result;
    private double price;

    public PlayerBuyPlotEvent(final PlotPlayer<?> plotPlayer, final Plot plot, @NonNegative final double price) {
        super(plotPlayer, plot);
        this.price = price;
    }


    /**
     * Sets the price required to buy the plot.
     *
     * @param price the new price.
     * @since 7.3.2
     */
    public void setPrice(@NonNegative final double price) {
        //noinspection ConstantValue - the annotation does not ensure a non-negative runtime value
        if (price < 0) {
            throw new IllegalArgumentException("price must be non-negative");
        }
        this.price = price;
    }

    /**
     * Returns the currently set price required to buy the plot.
     *
     * @return the price.
     * @since 7.3.2
     */
    public @NonNegative double price() {
        return price;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventResult(@Nullable final Result eventResult) {
        this.result = eventResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Result getEventResult() {
        return this.result;
    }

}
