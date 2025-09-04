package com.plotsquared.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "plot_rating")
@IdClass(PlotRatingId.class)
@NamedQueries({
        @NamedQuery(name = "PlotRating.findByPlot", query = "SELECT r FROM PlotRatingEntity r WHERE r.plotId = :plotId"),
        @NamedQuery(name = "PlotRating.upsert", query = "UPDATE PlotRatingEntity r SET r.rating = :rating WHERE r.plotId = :plotId AND r.player = :player"),
        @NamedQuery(name = "PlotRating.updateValue", query = "UPDATE PlotRatingEntity r SET r.rating = :rating WHERE r.plotId = :plotId AND r.player = :player")
})
public class PlotRatingEntity {
    @Id @Column(name = "plot_plot_id")
    private Long plotId;
    @Id
    @Column(length = 40)
    private String player;
    @Column(nullable = false)
    private Integer rating;

    public PlotRatingEntity() {}

    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
