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
package com.intellectualcrafters.plot.events;

import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

/**
 * @author Empire92
 */
public class PlotMergeEvent extends Event implements Cancellable {
    private static HandlerList handlers = new HandlerList();
    private final ArrayList<PlotId> plots;
    private boolean cancelled;
    private Plot plot;
    private World world;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world World in which the event occurred
     * @param plot  Plot that was merged
     * @param plots A list of plots involved in the event
     */
    public PlotMergeEvent(final World world, final Plot plot, final ArrayList<PlotId> plots) {
        this.plots = plots;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plots being added;
     *
     * @return Plot
     */
    public ArrayList<PlotId> getPlots() {
        return this.plots;
    }

    /**
     * Get the main plot
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean b) {
        this.cancelled = b;
    }
}
