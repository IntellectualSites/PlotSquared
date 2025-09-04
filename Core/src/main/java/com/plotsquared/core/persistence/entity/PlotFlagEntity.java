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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "plot_flags", uniqueConstraints = @UniqueConstraint(columnNames = {"plot_id","flag"}))
@NamedQueries({
        @NamedQuery(name = "PlotFlag.findByPlot", query = "SELECT f FROM PlotFlagEntity f WHERE f.plot.id = :plotId"),
        @NamedQuery(name = "PlotFlag.findByPlotAndName", query = "SELECT f FROM PlotFlagEntity f WHERE f.plot.id = :plotId AND f.flag = :flag"),
        @NamedQuery(name = "PlotFlag.deleteByPlotAndName", query = "DELETE FROM PlotFlagEntity f WHERE f.plot.id = :plotId AND f.flag = :flag")
})
public class PlotFlagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id", nullable = false)
    private PlotEntity plot;
    @Column(length = 64)
    private String flag;
    @Column(length = 512)
    private String value;

    public PlotFlagEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlotEntity getPlot() { return plot; }
    public void setPlot(PlotEntity plot) { this.plot = plot; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
