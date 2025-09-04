package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotFlagEntity;

import java.util.List;
import java.util.Optional;

public interface PlotFlagRepository {
    List<PlotFlagEntity> findByPlotId(long plotId);
    Optional<PlotFlagEntity> findByPlotAndName(long plotId, String flagName);
    void save(PlotFlagEntity entity);
    void deleteByPlotAndName(long plotId, String flagName);
}
