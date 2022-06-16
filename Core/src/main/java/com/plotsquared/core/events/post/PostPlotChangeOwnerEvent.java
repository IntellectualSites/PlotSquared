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

import com.plotsquared.core.events.PlotPlayerEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * Called after the owner of a plot was updated.
 *
 * @since 6.2.0
 */
public class PostPlotChangeOwnerEvent extends PlotPlayerEvent {

    @Nullable
    private final UUID oldOwner;

    /**
     * Instantiate a new PlotChangedOwnerEvent.
     *
     * @param initiator The player who executed the owner change.
     * @param plot      The plot which owner was changed.
     * @param oldOwner  The previous owner - if present, otherwise {@code null}.
     */
    public PostPlotChangeOwnerEvent(final PlotPlayer<?> initiator, final Plot plot, @Nullable UUID oldOwner) {
        super(initiator, plot);
        this.oldOwner = oldOwner;
    }

    /**
     * @return the old owner of the plot - if present, otherwise {@code null}.
     */
    public @Nullable UUID getOldOwner() {
        return oldOwner;
    }

    /**
     * @return {@code true} if the plot had an owner, {@code false} otherwise.
     * @see #getOldOwner()
     */
    public boolean hasOldOwner() {
        return getOldOwner() != null;
    }

    /**
     * @return {@code true} if the plot now has an owner, {@code false} otherwise.
     * @see Plot#hasOwner()
     */
    public boolean hasNewOwner() {
        return getPlot().hasOwner();
    }

}
