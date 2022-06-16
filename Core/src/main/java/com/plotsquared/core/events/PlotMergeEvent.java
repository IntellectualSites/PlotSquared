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

import com.plotsquared.core.location.Direction;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event called when several plots are merged
 * {@inheritDoc}
 */
public final class PlotMergeEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private final String world;
    private final PlotPlayer<?> player;
    private Direction dir;
    private int max;
    private Result eventResult;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world  World in which the event occurred
     * @param plot   Plot that was merged
     * @param dir    The direction of the merge
     * @param max    Max merge size
     * @param player The player attempting the merge
     */
    public PlotMergeEvent(
            final @NonNull String world, final @NonNull Plot plot,
            final @NonNull Direction dir, final int max, final PlotPlayer<?> player
    ) {
        super(player, plot);
        this.world = world;
        this.dir = dir;
        this.max = max;
        this.player = player;
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

    public Direction getDir() {
        return this.dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public PlotPlayer<?> getPlayer() {
        return this.player;
    }

}
