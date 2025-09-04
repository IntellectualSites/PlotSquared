package com.plotsquared.core.persistence.repository.api;

import com.plotsquared.core.persistence.entity.PlayerMetaEntity;

import java.util.List;

public interface PlayerMetaRepository {
    List<PlayerMetaEntity> findByUuid(String uuid);
    void put(String uuid, String key, byte[] value);
    void delete(String uuid, String key);
}
