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
public class PlayerPlotAddRemoveEvent extends PlotEvent {

    private final Reason reason;
    private final PlotPlayer<?> initiator;
    private final UUID player;

    protected PlayerPlotAddRemoveEvent(final PlotPlayer<?> initiator, Plot plot, final UUID player, final Reason reason) {
        super(plot);
        this.initiator = initiator;
        this.player = player;
        this.reason = reason;
    }

    /**
     * The player to be added/denied/removed/trusted.
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action. May be null (if a player, or a {@link com.plotsquared.core.player.ConsolePlayer}.
     *
     * @return PlotPlayer
     */
    @Nullable
    public PlotPlayer<?> getInitiator() {
        return this.initiator;
    }

    /**
     * Get the reason the player is being added/removed/denied/trusted
     *
     * @return Reason
     */
    public Reason getReason() {
        return this.reason;
    }

    public enum Reason {
        /**
         * If a plot merge caused the player to be added/removed/denied/trusted
         */
        MERGE,
        /**
         * If a command caused the player to be added/removed/denied/trusted
         */
        COMMAND,
        /**
         * If something unknown caused the player to be added/removed/denied/trusted
         */
        UNKNOWN
    }

}
