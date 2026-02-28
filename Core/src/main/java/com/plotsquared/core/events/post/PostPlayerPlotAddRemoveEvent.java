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
package com.plotsquared.core.events.post;

import com.plotsquared.core.events.PlayerPlotAddRemoveEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

import java.util.UUID;

/**
 * Parent class for events covering players being added/removed to added/trusted/denied lists.
 *
 * @since TODO
 */
public sealed class PostPlayerPlotAddRemoveEvent extends PlayerPlotAddRemoveEvent permits PostPlayerPlotAddedEvent,
        PostPlayerPlotDeniedEvent, PostPlayerPlotTrustedEvent {

    private final boolean added;

    protected PostPlayerPlotAddRemoveEvent(
            final PlotPlayer<?> initiator,
            final Plot plot,
            final UUID player,
            final Reason reason,
            boolean added
    ) {
        super(initiator, plot, player, reason);
        this.added = added;
    }

    /**
     * Get if the player was added to a list, or removed.
     */
    public boolean added() {
        return added;
    }


}
