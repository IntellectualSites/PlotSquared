package com.plotsquared.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "plot_helpers")
@IdClass(PlotUserId.class)
@NamedQueries({
        @NamedQuery(name = "PlotHelper.delete", query = "DELETE FROM PlotMembershipEntity e WHERE e.plotId = :plotId AND e.userUuid = :uuid"),
        @NamedQuery(name = "PlotHelper.findUsers", query = "SELECT e.userUuid FROM PlotMembershipEntity e WHERE e.plotId = :plotId")
})
public class PlotMembershipEntity {
    @Id
    @Column(name = "plot_plot_id")
    private Long plotId;
    @Id @Column(name = "user_uuid", length = 40)
    private String userUuid;

    public PlotMembershipEntity() {}

    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }
}
