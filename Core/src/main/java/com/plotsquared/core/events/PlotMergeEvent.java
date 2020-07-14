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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.events;

import com.plotsquared.core.location.Direction;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import lombok.Getter;
import lombok.Setter;
import javax.annotation.Nonnull;

/**
 * Event called when several plots are merged
 * {@inheritDoc}
 */
public final class PlotMergeEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    @Getter private final String world;
    @Getter @Setter private Direction dir;
    @Getter @Setter private int max;
    private Result eventResult;
    @Getter private PlotPlayer player;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world  World in which the event occurred
     * @param plot   Plot that was merged
     * @param dir    The direction of the merge
     * @param max    Max merge size
     * @param player The player attempting the merge
     */
    public PlotMergeEvent(@Nonnull final String world, @Nonnull final Plot plot,
        @Nonnull final Direction dir, final int max, PlotPlayer player) {
        super(player, plot);
        this.world = world;
        this.dir = dir;
        this.max = max;
        this.player = player;
    }


    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
