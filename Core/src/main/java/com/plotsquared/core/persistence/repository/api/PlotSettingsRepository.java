package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotSettingsEntity;

import java.util.Optional;

public interface PlotSettingsRepository {
    Optional<PlotSettingsEntity> findByPlotId(long plotId);
    void save(PlotSettingsEntity settings);
    void deleteByPlotId(long plotId);
}
