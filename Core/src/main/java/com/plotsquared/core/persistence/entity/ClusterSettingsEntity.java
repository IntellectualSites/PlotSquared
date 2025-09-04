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
