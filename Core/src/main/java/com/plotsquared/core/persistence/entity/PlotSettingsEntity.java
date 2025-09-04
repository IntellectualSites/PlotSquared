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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plot_settings")
@jakarta.persistence.NamedQueries({
        @jakarta.persistence.NamedQuery(name = "PlotSettings.updateAlias", query = "UPDATE PlotSettingsEntity s SET s.alias = :alias WHERE s.id = :plotId"),
        @jakarta.persistence.NamedQuery(name = "PlotSettings.updatePosition", query = "UPDATE PlotSettingsEntity s SET s.position = :pos WHERE s.id = :plotId"),
        @jakarta.persistence.NamedQuery(name = "PlotSettings.updateMerged", query = "UPDATE PlotSettingsEntity s SET s.merged = :merged WHERE s.id = :plotId")
})
public class PlotSettingsEntity {
    @Id
    @Column(name = "plot_plot_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "plot_plot_id")
    private PlotEntity plot;

    @Column(length = 45)
    private String biome;
    @Column(name = "rain")
    private Integer rain;
    @Column(name = "custom_time")
    private Boolean customTime;
    @Column(name = "time")
    private Integer time;
    @Column(name = "deny_entry")
    private Boolean denyEntry;
    @Column(length = 50)
    private String alias;
    @Column(name = "merged")
    private Integer merged;
    @Column(length = 50)
    private String position;

    public PlotSettingsEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlotEntity getPlot() { return plot; }
    public void setPlot(PlotEntity plot) { this.plot = plot; }

    public String getBiome() { return biome; }
    public void setBiome(String biome) { this.biome = biome; }

    public Integer getRain() { return rain; }
    public void setRain(Integer rain) { this.rain = rain; }

    public Boolean getCustomTime() { return customTime; }
    public void setCustomTime(Boolean customTime) { this.customTime = customTime; }

    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }

    public Boolean getDenyEntry() { return denyEntry; }
    public void setDenyEntry(Boolean denyEntry) { this.denyEntry = denyEntry; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public Integer getMerged() { return merged; }
    public void setMerged(Integer merged) { this.merged = merged; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
