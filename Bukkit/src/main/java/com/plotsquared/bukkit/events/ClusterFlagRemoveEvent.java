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

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.PlotCluster;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a flag is removed from a plot
 *


 */
public class ClusterFlagRemoveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final PlotCluster cluster;
    private final Flag flag;
    private boolean cancelled;

    /**
     * PlotFlagRemoveEvent: Called when a flag is removed from a plot
     *
     * @param flag Flag that was removed
     * @param cluster PlotCluster from which the flag was removed
     */
    public ClusterFlagRemoveEvent(Flag flag, PlotCluster cluster) {
        this.cluster = cluster;
        this.flag = flag;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the cluster involved
     *
     * @return PlotCluster
     */
    public PlotCluster getCluster() {
        return this.cluster;
    }

    /**
     * Get the flag involved
     *
     * @return Flag
     */
    public Flag getFlag() {
        return this.flag;
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
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
