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

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

import java.util.UUID;

/**
 * Called when a player is denied from a plot
 *
 * @since TODO
 */
public final class PostPlayerPlotDeniedEvent extends PostPlayerPlotAddRemoveEvent {

    /**
     * PlayerPlotDeniedEvent: Called when the denied UUID list is modified for a plot.
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was denied/un-denied
     * @param added     {@code true} of add to deny list, {@code false} if removed
     * @param reason    The reason for the deny/remove
     */
    public PostPlayerPlotDeniedEvent(PlotPlayer<?> initiator, Plot plot, UUID player, boolean added, Reason reason) {
        super(initiator, plot, player, reason, added);
    }

}
