package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotSettingsEntity;

import java.util.Optional;

/**
 * Repository for persisting and retrieving per-plot settings.
 */
public interface PlotSettingsRepository {
    /**
     * Retrieves the settings for the given plot if available.
     *
     * @param plotId the plot identifier
     * @return Optional with the settings if present; otherwise empty
     */
    Optional<PlotSettingsEntity> findByPlotId(long plotId);

    /**
     * Persists the provided settings entity (insert or update).
     *
     * @param settings the settings entity to save
     */
    void save(PlotSettingsEntity settings);

    /**
     * Deletes the settings associated with the specified plot id. No-op if absent.
     *
     * @param plotId the plot identifier
     */
    void deleteByPlotId(long plotId);
}
