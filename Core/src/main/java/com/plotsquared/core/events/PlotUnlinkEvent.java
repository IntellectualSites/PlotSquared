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

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event called when several merged plots are unlinked
 * {@inheritDoc}
 */
public final class PlotUnlinkEvent extends PlotEvent implements CancellablePlotEvent {

    private final PlotArea area;
    boolean createRoad;
    boolean createSign;
    REASON reason;
    private Result eventResult = Result.ACCEPT;

    /**
     * PlotUnlinkEvent: Called when a mega plot is unlinked
     *
     * @param area       The applicable plot area
     * @param plot       The plot being unlinked from
     * @param createRoad Whether to regenerate the road
     * @param createSign Whether to regenerate signs
     * @param reason     The {@link REASON} for the unlink
     */
    public PlotUnlinkEvent(
            final @NonNull PlotArea area, Plot plot, boolean createRoad,
            boolean createSign, REASON reason
    ) {
        super(plot);
        this.area = area;
        this.createRoad = createRoad;
        this.createSign = createSign;
        this.reason = reason;
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }

    public PlotArea getArea() {
        return this.area;
    }

    public boolean isCreateRoad() {
        return this.createRoad;
    }

    public void setCreateRoad(boolean createRoad) {
        this.createRoad = createRoad;
    }

    public boolean isCreateSign() {
        return this.createSign;
    }

    public void setCreateSign(boolean createSign) {
        this.createSign = createSign;
    }

    public REASON getReason() {
        return this.reason;
    }

    public enum REASON {
        NEW_OWNER,
        PLAYER_COMMAND,
        CLEAR,
        DELETE,
        EXPIRE_DELETE
    }

}
