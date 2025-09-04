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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "plot_denied")
@IdClass(PlotUserId.class)
@NamedQueries({
        @NamedQuery(name = "PlotDenied.delete", query = "DELETE FROM PlotDeniedEntity e WHERE e.plotId = :plotId AND e.userUuid = :uuid"),
        @NamedQuery(name = "PlotDenied.findUsers", query = "SELECT e.userUuid FROM PlotDeniedEntity e WHERE e.plotId = :plotId"),
        @NamedQuery(name = "PlotDenied.deleteAll", query = "DELETE FROM PlotDeniedEntity e WHERE e.plotId = :plotId")
})
public class PlotDeniedEntity {
    @Id
    @Column(name = "plot_plot_id")
    private Long plotId;
    @Id @Column(name = "user_uuid", length = 40)
    private String userUuid;

    public PlotDeniedEntity() {}

    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }
}
