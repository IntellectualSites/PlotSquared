package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlotEntity;

import java.util.List;
import java.util.Optional;

public interface PlotRepository {
    Optional<PlotEntity> findById(long id);
    Optional<PlotEntity> findByWorldAndId(String world, int x, int z);
    List<PlotEntity> findByOwner(String ownerUuid);
    List<PlotEntity> findByWorld(String world);
    void save(PlotEntity plot);
    void deleteById(long id);
}
