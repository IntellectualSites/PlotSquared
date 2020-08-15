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
 *                  Copyright (C) 2020 IntellectualSites
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

import com.plotsquared.core.command.Claim;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;

import javax.annotation.Nullable;

public class PlayerAutoPlotEvent extends PlotEvent implements CancellablePlotEvent {

    private Result eventResult;
    private String schematic;
    private final PlotPlayer<?> player;
    private final PlotArea plotArea;
    private int size_x;
    private int size_z;

    /**
     * PlayerAutoPlotEvent: called when a player attempts to auto claim a plot.
     *
     * @param player    The player attempting to auto claim
     * @param plotArea  The applicable plot area
     * @param schematic The schematic defined or null
     * @param size_x    The size of the auto area
     * @param size_z    The size of the auto area
     */
    public PlayerAutoPlotEvent(PlotPlayer<?> player, PlotArea plotArea, @Nullable String schematic,
        int size_x, int size_z) {
        super(null);
        this.player = player;
        this.plotArea = plotArea;
        this.schematic = schematic;
        this.size_x = size_x;
        this.size_z = size_z;
    }

    /**
     * Obtain the schematic string as used by the {@link Claim} command or null.
     *
     * @return schematic string
     */
    @Nullable public String getSchematic() {
        return this.schematic;
    }

    /**
     * Set the schematic string used in the claim.
     */
    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }

    public PlotPlayer<?> getPlayer() {
        return this.player;
    }

    public PlotArea getPlotArea() {
        return this.plotArea;
    }

    public int getSize_x() {
        return this.size_x;
    }

    public int getSize_z() {
        return this.size_z;
    }

    public void setSize_x(int size_x) {
        this.size_x = size_x;
    }

    public void setSize_z(int size_z) {
        this.size_z = size_z;
    }
}
