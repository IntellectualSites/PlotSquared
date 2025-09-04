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
