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
package com.plotsquared.core.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class PlotRatingId implements Serializable {
    private Long plotId;
    private String player;

    public PlotRatingId() {}

    public PlotRatingId(Long plotId, String player) {
        this.plotId = plotId;
        this.player = player;
    }

    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotRatingId that = (PlotRatingId) o;
        return Objects.equals(plotId, that.plotId) && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plotId, player);
    }
}
