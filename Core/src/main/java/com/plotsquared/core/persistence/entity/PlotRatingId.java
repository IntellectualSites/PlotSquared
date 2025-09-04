package com.plotsquared.core.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class PlotRatingId implements Serializable {
    private Long plotId;
    private String player;

    public PlotRatingId() {}

    public PlotRatingId(Long plotId, String player) {
        this.plotId = plotId;
        this.player = player;
    }

    public Long getPlotId() { return plotId; }
    public void setPlotId(Long plotId) { this.plotId = plotId; }
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotRatingId that = (PlotRatingId) o;
        return Objects.equals(plotId, that.plotId) && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plotId, player);
    }
}
