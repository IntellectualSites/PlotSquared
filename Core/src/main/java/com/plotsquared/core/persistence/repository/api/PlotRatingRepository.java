package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotRatingEntity;

import java.util.List;

public interface PlotRatingRepository {
    List<PlotRatingEntity> findByPlotId(long plotId);
    void upsert(long plotId, String playerUuid, int rating);
}
