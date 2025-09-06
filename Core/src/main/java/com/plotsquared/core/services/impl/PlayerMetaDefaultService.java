/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.services.impl;

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
