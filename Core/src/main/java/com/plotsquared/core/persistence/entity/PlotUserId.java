package com.plotsquared.core.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class PlotUserId implements Serializable {
    private Long plotId;
    private String userUuid;

    public PlotUserId() {}

    public PlotUserId(Long plotId, String userUuid) {
        this.plotId = plotId;
        this.userUuid = userUuid;
    }

    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotUserId that = (PlotUserId) o;
        return Objects.equals(plotId, that.plotId) && Objects.equals(userUuid, that.userUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plotId, userUuid);
    }
}
