////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * @author Citymonstret
 * @author Empire92
 */
public class PlayerPlotDeniedEvent extends PlotEvent {

    private static HandlerList handlers = new HandlerList();
    private final Player initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotDeniedEvent: Called when the denied UUID list is modified for a plot
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was denied/un-denied
     * @param added     true of add to deny list, false if removed
     */
    public PlayerPlotDeniedEvent(final Player initiator, final Plot plot, final UUID player, final boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * If a user was added
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }

    /**
     * The player added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action
     *
     * @return Player
     */
    public Player getInitiator() {
        return this.initiator;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
