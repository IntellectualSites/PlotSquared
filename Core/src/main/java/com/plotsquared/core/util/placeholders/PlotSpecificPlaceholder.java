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

package com.plotsquared.core.util.placeholders;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

import javax.annotation.Nonnull;

/**
 * A {@link Placeholder placeholder} that requires a {@link com.plotsquared.core.plot.Plot plot}
 */
public abstract class PlotSpecificPlaceholder extends Placeholder {

    public PlotSpecificPlaceholder(@Nonnull final String key) {
        super(key);
    }

    @Override @Nonnull public final String getValue(@Nonnull final PlotPlayer<?> player) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return "";
        }
        return this.getValue(player, plot);
    }

    /**
     * Get the value of the placeholder for the {@link PlotPlayer player} in a specific {@link Plot plot}
     *
     * @param player Player that the placeholder is evaluated for
     * @param plot   Plot that the player is in
     * @return Placeholder value, or {@code ""} if the placeholder does not apply
     */
    @Nonnull public abstract String getValue(@Nonnull final PlotPlayer<?> player,
        @Nonnull final Plot plot);

}
