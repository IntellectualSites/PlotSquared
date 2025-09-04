package com.plotsquared.core.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plot")
@NamedQueries({
        @NamedQuery(name = "Plot.findByWorldAndId", query = "SELECT p FROM PlotEntity p WHERE p.world = :world AND p.plotIdX = :x AND p.plotIdZ = :z"),
        @NamedQuery(name = "Plot.findByOwner", query = "SELECT p FROM PlotEntity p WHERE p.owner = :owner"),
        @NamedQuery(name = "Plot.findByWorld", query = "SELECT p FROM PlotEntity p WHERE p.world = :world"),
        @NamedQuery(
                name = "Plot.updateXANDZ",
                query = "UPDATE PlotEntity p SET p.plotIdX = :x, p.plotIdZ = :z  WHERE p.id = :id"
        ),
        @NamedQuery(name = "Plot.findByXAndZAndWorld", query = "SELECT p FROM PlotEntity p WHERE p.plotIdX = :x AND p.plotIdZ = :z AND " +
                "world = :world order by timestamp asc "),
        @NamedQuery(
                name = "Plot.movePlot",
                query = "UPDATE PlotEntity p SET p.plotIdX = :plotIdX, p.plotIdZ = :plotIdZ, p.world = :world WHERE p.id = :id"
        ),
        @NamedQuery(name = "Plot.setOwner", query = "UPDATE PlotEntity p SET p.owner = :owner WHERE p.world = :world AND p.plotIdX = :x AND p.plotIdZ = :z"),
})
public class PlotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "plot_id_x", nullable = false)
    private int plotIdX;
    @Column(name = "plot_id_z", nullable = false)
    private int plotIdZ;
    @Column(length = 40, nullable = false)
    private String owner;
    @Column(length = 45, nullable = false)
    private String world;
    @Column(name = "timestamp", insertable = false, updatable = false)
    private java.sql.Timestamp timestamp;

    @OneToOne(mappedBy = "plot", cascade = CascadeType.ALL, optional = true, fetch = FetchType.LAZY)
    private PlotSettingsEntity settings;

    public PlotEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPlotIdX() {
        return plotIdX;
    }

    public void setPlotIdX(int plotIdX) {
        this.plotIdX = plotIdX;
    }

    public int getPlotIdZ() {
        return plotIdZ;
    }

    public void setPlotIdZ(int plotIdZ) {
        this.plotIdZ = plotIdZ;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public java.sql.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.sql.Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public PlotSettingsEntity getSettings() {
        return settings;
    }

    public void setSettings(PlotSettingsEntity settings) {
        this.settings = settings;
    }
}
