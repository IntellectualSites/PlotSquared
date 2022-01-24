/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *               Copyright (C) 2014 - 2022 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.events;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

/**
 * Called when a player teleports to a plot
 */
public class PlayerTeleportToPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private final Location from;
    private final TeleportCause cause;
    private Result eventResult;

    /**
     * @deprecated use {@link PlayerTeleportToPlotEvent#PlayerTeleportToPlotEvent(PlotPlayer, Location, Plot, TeleportCause)}.
     * You should not be creating events in the first place.
     */
    @Deprecated(forRemoval = true, since = "6.1.0")
    public PlayerTeleportToPlotEvent(PlotPlayer<?> player, Location from, Plot plot) {
        this(player, from, plot, TeleportCause.UNKNOWN);
    }

    /**
     * PlayerTeleportToPlotEvent: Called when a player teleports to a plot
     *
     * @param player That was teleported
     * @param from   Start location
     * @param plot   Plot to which the player was teleported
     * @param cause  Why the teleport is being completed
     * @since 6.1.0
     */
    public PlayerTeleportToPlotEvent(PlotPlayer<?> player, Location from, Plot plot, TeleportCause cause) {
        super(player, plot);
        this.from = from;
        this.cause = cause;
    }

    /**
     * Get the teleport cause
     *
     * @return TeleportCause
     * @since 6.1.0
     */
    public TeleportCause getCause() {
        return cause;
    }

    /**
     * Get the from location
     *
     * @return Location
     */
    public Location getFrom() {
        return this.from;
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
