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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Parent class for the varies events regarding a player being added/removed/denied/trusted
 *
 * @since TODO
 */
public class PlayerPlotAddRemoveCancellableEvent extends PlayerPlotAddRemoveEvent implements CancellablePlotEvent {

    private Result eventResult;

    protected PlayerPlotAddRemoveCancellableEvent(
            final PlotPlayer<?> initiator,
            Plot plot,
            final UUID player,
            final Reason reason
    ) {
        super(initiator, plot, player, reason);
    }

    @Override
    public @Nullable Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(@Nullable final Result eventResult) {
        this.eventResult = eventResult;
    }

}
