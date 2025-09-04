package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.ClusterEntity;

import java.util.List;
import java.util.Optional;

public interface ClusterRepository {
    Optional<ClusterEntity> findById(long id);
    Optional<ClusterEntity> findByWorldAndBounds(String world, int x, int z);
    List<ClusterEntity> findByWorld(String world);
    void save(ClusterEntity cluster);
    void deleteById(long id);
}
