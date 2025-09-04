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
        @NamedQuery(name = "PlotDenied.findUsers", query = "SELECT e.userUuid FROM PlotDeniedEntity e WHERE e.plotId = :plotId")
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
