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
@Table(name = "cluster_settings")
@jakarta.persistence.NamedQueries({
        @jakarta.persistence.NamedQuery(name = "ClusterSettings.updatePosition", query = "UPDATE ClusterSettingsEntity s SET s.position = :pos WHERE s.id = :clusterId")
})
public class ClusterSettingsEntity {
    @Id
    @Column(name = "cluster_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "cluster_id")
    private ClusterEntity cluster;

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

    public ClusterSettingsEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClusterEntity getCluster() {
        return cluster;
    }

    public void setCluster(ClusterEntity cluster) {
        this.cluster = cluster;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }

    public Integer getRain() {
        return rain;
    }

    public void setRain(Integer rain) {
        this.rain = rain;
    }

    public Boolean getCustomTime() {
        return customTime;
    }

    public void setCustomTime(Boolean customTime) {
        this.customTime = customTime;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Boolean getDenyEntry() {
        return denyEntry;
    }

    public void setDenyEntry(Boolean denyEntry) {
        this.denyEntry = denyEntry;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getMerged() {
        return merged;
    }

    public void setMerged(Integer merged) {
        this.merged = merged;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
