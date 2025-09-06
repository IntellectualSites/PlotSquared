package com.plotsquared.core.services.impl;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.persistence.entity.PlayerMetaEntity;
import com.plotsquared.core.persistence.repository.api.PlayerMetaRepository;
import com.plotsquared.core.services.api.PlayerMetaService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerMetaDefaultService implements PlayerMetaService {

    private final PlayerMetaRepository repository;

    @Inject
    public PlayerMetaDefaultService(final PlayerMetaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addPersistentMeta(final @NotNull UUID uuid, final @NotNull String key, final byte @NotNull [] meta, final boolean delete) {
        if (delete) {
            this.repository.delete(uuid.toString(), key);
        } else {
            this.repository.put(uuid.toString(), key, meta);
        }
    }

    @Override
    public void getPersistentMeta(final @NotNull UUID uuid, final @NotNull Consumer<Map<String, byte[]>> result) {
        Map<String, byte[]> map = new HashMap<>();
        for (PlayerMetaEntity e : this.repository.findByUuid(uuid.toString())) {
            map.put(e.getKey(), e.getPlayerMetaValue());
        }
        result.accept(map);
    }

    @Override
    public void removePersistentMeta(final UUID uuid, final @NotNull String key) {
        this.repository.delete(uuid.toString(), key);
    }

}
