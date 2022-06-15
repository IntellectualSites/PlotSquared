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
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class PlotChangeOwnerEvent extends PlotEvent implements CancellablePlotEvent {

    private final PlotPlayer<?> initiator;
    @Nullable
    private final UUID oldOwner;
    private final boolean hasOldOwner;
    @Nullable
    private UUID newOwner;
    private Result eventResult;

    /**
     * PlotChangeOwnerEvent: Called when a plot's owner is change.
     *
     * @param initiator   The player attempting to set the plot's owner
     * @param plot        The plot having its owner changed
     * @param oldOwner    The old owner of the plot or null
     * @param newOwner    The new owner of the plot or null
     * @param hasOldOwner If the plot has an old owner
     */
    public PlotChangeOwnerEvent(
            PlotPlayer<?> initiator, Plot plot, @Nullable UUID oldOwner,
            @Nullable UUID newOwner, boolean hasOldOwner
    ) {
        super(plot);
        this.initiator = initiator;
        this.newOwner = newOwner;
        this.oldOwner = oldOwner;
        this.hasOldOwner = hasOldOwner;
    }

    /**
     * Get the PlotId.
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name.
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
    }

    /**
     * Get the change-owner initiator
     *
     * @return Player
     */
    public PlotPlayer<?> getInitiator() {
        return this.initiator;
    }

    /**
     * Get the old owner of the plot. Null if not exists.
     *
     * @return UUID
     */
    public @Nullable UUID getOldOwner() {
        return this.oldOwner;
    }

    /**
     * Get the new owner of the plot
     *
     * @return UUID
     */
    public @Nullable UUID getNewOwner() {
        return this.newOwner;
    }


    /**
     * Set the new owner of the plot. Null for no owner.
     *
     * @param newOwner the new owner or null
     */
    public void setNewOwner(@Nullable UUID newOwner) {
        this.newOwner = newOwner;
    }

    /**
     * Get if the plot had an old owner
     *
     * @return boolean
     */
    public boolean hasOldOwner() {
        return this.hasOldOwner;
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }

}
