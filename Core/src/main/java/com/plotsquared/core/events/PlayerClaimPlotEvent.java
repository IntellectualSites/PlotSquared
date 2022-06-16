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

import com.plotsquared.core.command.Claim;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PlayerClaimPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private Result eventResult;
    private String schematic;

    /**
     * PlayerClaimPlotEvent: Called when a plot is claimed.
     *
     * @param player    Player that claimed the plot
     * @param plot      Plot that was claimed
     * @param schematic The schematic defined or null
     */
    public PlayerClaimPlotEvent(PlotPlayer<?> player, Plot plot, @Nullable String schematic) {
        super(player, plot);
        this.schematic = schematic;
    }

    /**
     * Obtain the schematic string as used by the {@link Claim} command or null.
     *
     * @return schematic string
     */
    public @Nullable String getSchematic() {
        return this.schematic;
    }

    /**
     * Set the schematic string used in the claim.
     *
     * @param schematic the schematic name
     */
    public void setSchematic(String schematic) {
        this.schematic = schematic;
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
